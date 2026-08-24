package com.alperensarac.projectmanagementkotlin.core.network.authenticator

import com.alperensarac.projectmanagementkotlin.core.auth.session.SessionEventBus
import com.alperensarac.projectmanagementkotlin.core.datastore.token.TokenStorage
import com.alperensarac.projectmanagementkotlin.data.remote.api.RefreshApi
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.RefreshTokenRequestDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Backend'den 401 response alındığında access token yenilemeyi dener.
 *
 * Güvenlik kuralları:
 *
 * 1. Aynı anda yalnızca bir refresh isteği çalışır.
 * 2. Başka bir istek tokenı zaten yenilediyse tekrar refresh yapılmaz.
 * 3. Refresh endpoint'i için authenticator çalıştırılmaz.
 * 4. Aynı request sonsuz sayıda yeniden gönderilmez.
 * 5. Refresh başarısız olursa bütün tokenlar temizlenir.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val refreshApi: RefreshApi,
    private val sessionEventBus: SessionEventBus
) : Authenticator {

    /**
     * Authenticator API'si senkron olduğu için suspend token işlemlerini
     * kontrollü şekilde runBlocking içerisinde yürütüyoruz.
     *
     * RefreshApi farklı bir OkHttpClient kullandığı için aynı client
     * dispatcher'ında recursive authenticator döngüsü oluşmaz.
     */
    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {
        /*
         * Refresh endpoint'i 401 döndürürse yeni bir refresh denemiyoruz.
         */
        if (isRefreshRequest(response.request)) {
            clearSessionBlocking()
            return null
        }

        /*
         * Aynı request daha önce yeniden denenmişse sonsuz döngüyü keseriz.
         */
        if (responseCount(response) >= MAX_AUTHENTICATION_ATTEMPTS) {
            clearSessionBlocking()
            return null
        }

        return runBlocking {
            refreshMutex.withLock {
                authenticateInsideMutex(response)
            }
        }
    }

    /**
     * Mutex içerisindeki asıl token kontrolü ve refresh işlemidir.
     */
    private suspend fun authenticateInsideMutex(
        response: Response
    ): Request? {
        val tokenUsedByFailedRequest =
            extractBearerToken(
                response.request.header(AUTHORIZATION_HEADER)
            )

        val latestAccessToken =
            tokenStorage.getAccessToken()

        /*
         * Bu request 401 aldıktan sonra başka bir request tokenı yenilemiş
         * olabilir.
         *
         * Başarısız requestte kullanılan token ile bellekteki son token
         * farklıysa yeniden refresh yapmadan güncel token ile tekrar deneriz.
         */
        if (
            !latestAccessToken.isNullOrBlank() &&
            latestAccessToken != tokenUsedByFailedRequest
        ) {
            return buildRetriedRequest(
                originalRequest = response.request,
                accessToken = latestAccessToken
            )
        }

        val tokenSnapshot =
            tokenStorage.getTokenSnapshot()

        val refreshToken =
            tokenSnapshot.refreshToken

        if (refreshToken.isNullOrBlank()) {
            clearSession()
            return null
        }

        val refreshResponse = runCatching {
            refreshApi.refresh(
                request = RefreshTokenRequestDto(
                    refreshToken = refreshToken
                )
            )
        }.getOrNull()

        val refreshedSession =
            refreshResponse?.data

        if (
            refreshResponse == null ||
            !refreshResponse.success ||
            refreshedSession == null ||
            refreshedSession.accessToken.isBlank() ||
            refreshedSession.refreshToken.isBlank()
        ) {
            clearSession()
            return null
        }

        /*
         * Backend refresh token rotation uyguladığı için access ve refresh
         * token birlikte kaydedilir.
         */
        tokenStorage.saveTokens(
            accessToken = refreshedSession.accessToken,
            refreshToken = refreshedSession.refreshToken,
            accessTokenExpiresAtUtc = refreshedSession.expiresAtUtc
        )

        return buildRetriedRequest(
            originalRequest = response.request,
            accessToken = refreshedSession.accessToken
        )
    }

    /**
     * Orijinal isteği yeni Bearer token ile tekrar oluşturur.
     */
    private fun buildRetriedRequest(
        originalRequest: Request,
        accessToken: String
    ): Request {
        return originalRequest
            .newBuilder()
            .header(
                AUTHORIZATION_HEADER,
                "$BEARER_PREFIX $accessToken"
            )
            .build()
    }

    /**
     * Authorization header içerisinden yalnızca token bölümünü çıkarır.
     */
    private fun extractBearerToken(
        authorizationHeader: String?
    ): String? {
        if (authorizationHeader.isNullOrBlank()) {
            return null
        }

        val prefix = "$BEARER_PREFIX "

        if (!authorizationHeader.startsWith(prefix, ignoreCase = true)) {
            return null
        }

        return authorizationHeader
            .substring(prefix.length)
            .trim()
            .takeIf { it.isNotEmpty() }
    }

    /**
     * Response zincirindeki toplam deneme sayısını hesaplar.
     */
    private fun responseCount(
        response: Response
    ): Int {
        var count = 1
        var currentResponse = response.priorResponse

        while (currentResponse != null) {
            count++
            currentResponse = currentResponse.priorResponse
        }

        return count
    }

    /**
     * Refresh endpoint'ine ait isteği tespit eder.
     */
    private fun isRefreshRequest(
        request: Request
    ): Boolean {
        return request.url.encodedPath.equals(
            other = REFRESH_ENDPOINT_PATH,
            ignoreCase = true
        )
    }

    /**
     * Suspend context içerisinden tokenları temizler ve UI olayını yayınlar.
     */
    private suspend fun clearSession() {
        tokenStorage.clearTokens()
        sessionEventBus.notifySessionExpired()
    }

    /**
     * Authenticator'ın erken çıkış noktalarında kullanılmak üzere
     * suspend temizleme işlemini senkron çalıştırır.
     */
    private fun clearSessionBlocking() {
        runBlocking {
            clearSession()
        }
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer"
        const val REFRESH_ENDPOINT_PATH = "/api/Auth/refresh"

        /*
         * İlk request ve yalnızca bir yeniden deneme.
         *
         * İkinci 401 sonrasında tekrar refresh yapılmaz.
         */
        const val MAX_AUTHENTICATION_ATTEMPTS = 2

        val refreshMutex = Mutex()
    }
}