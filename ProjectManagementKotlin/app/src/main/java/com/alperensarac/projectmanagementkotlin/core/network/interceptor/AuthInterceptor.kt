package com.alperensarac.projectmanagementkotlin.core.network.interceptor

import com.alperensarac.projectmanagementkotlin.core.datastore.token.TokenStorage
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Bellekte bulunan access token'ı korunan HTTP isteklerine ekler.
 *
 * Interceptor senkron çalıştığı için DataStore üzerinden suspend okuma
 * yapılmaz. Access token TokenStorage tarafından bellekte tutulmaktadır.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {
        val originalRequest = chain.request()

        /*
         * Request üzerinde zaten Authorization header bulunuyorsa
         * ikinci kez header eklemiyoruz.
         */
        if (originalRequest.header(AUTHORIZATION_HEADER) != null) {
            return chain.proceed(originalRequest)
        }

        /*
         * Login ve refresh endpointleri token gerektirmez.
         *
         * AuthApi authenticated Retrofit üzerinden oluşturulsa bile bu
         * endpointlere gereksiz Authorization header eklenmeyecektir.
         */
        if (isPublicAuthenticationEndpoint(originalRequest.url.encodedPath)) {
            return chain.proceed(originalRequest)
        }

        val accessToken = tokenStorage.getAccessToken()

        /*
         * Bellekte token yoksa isteği header eklemeden göndeririz.
         *
         * Endpoint korumalıysa backend 401 döndürecek ve authenticator
         * kayıtlı refresh token üzerinden oturumu yenilemeyi deneyecektir.
         */
        if (accessToken.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest
            .newBuilder()
            .header(
                AUTHORIZATION_HEADER,
                "$BEARER_PREFIX $accessToken"
            )
            .build()

        return chain.proceed(authenticatedRequest)
    }

    /**
     * Authorization header eklenmemesi gereken endpointleri belirler.
     */
    private fun isPublicAuthenticationEndpoint(
        encodedPath: String
    ): Boolean {
        return PUBLIC_AUTHENTICATION_PATHS.any { publicPath ->
            encodedPath.equals(
                other = publicPath,
                ignoreCase = true
            )
        }
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer"

        val PUBLIC_AUTHENTICATION_PATHS = setOf(
            "/api/Auth/login",
            "/api/Auth/register",
            "/api/Auth/refresh"
        )
    }
}