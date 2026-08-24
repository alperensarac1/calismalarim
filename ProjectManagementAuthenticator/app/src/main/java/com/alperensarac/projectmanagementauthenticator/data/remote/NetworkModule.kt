package com.alperensarac.projectmanagementauthenticator.data.remote

import com.alperensarac.projectmanagementauthenticator.BuildConfig
import com.alperensarac.projectmanagementauthenticator.data.remote.api.AuthenticatorApi
import com.alperensarac.projectmanagementauthenticator.data.remote.api.ChallengeVerificationApi
import com.alperensarac.projectmanagementauthenticator.data.remote.api.MainBackendApi

import com.google.gson.Gson
import com.google.gson.GsonBuilder

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit


/*
 * =========================================================
 * NETWORK MODULE
 * =========================================================
 */


/**
 * Uygulamadaki bütün HTTP ve WebSocket istemcilerini
 * merkezi olarak oluşturan singleton sınıftır.
 *
 * Bu sınıf üzerinden:
 *
 * - .NET ana backend API'si
 * - Python Authenticator API'si
 * - Challenge Verification API'si
 * - WebSocket OkHttpClient nesnesi
 *
 * oluşturulur.
 *
 * Uygulamada Hilt veya Koin gibi bir dependency
 * injection kütüphanesi kullanmadığımız için bu yapı
 * basit bir servis sağlayıcı olarak çalışır.
 */
object NetworkModule {

    /*
     * =====================================================
     * GSON
     * =====================================================
     */


    /**
     * Retrofit response ve request modellerinin JSON'a
     * dönüştürülmesinde kullanılan ortak Gson nesnesidir.
     *
     * serializeNulls sayesinde nullable request alanları
     * JSON içerisinde açık biçimde null olarak gönderilir.
     *
     * Örneğin challenge kararında:
     *
     * {
     *   "latitude": null,
     *   "longitude": null
     * }
     */
    val gson: Gson by lazy {
        GsonBuilder()
            .serializeNulls()
            .create()
    }


    /*
     * =====================================================
     * HTTP LOGGING
     * =====================================================
     */


    /**
     * Retrofit HTTP isteklerini Logcat üzerinde
     * gösterecek logging interceptor oluşturur.
     *
     * Debug modunda BODY seviyesinde log alınır.
     * Release modunda log kapatılır.
     *
     * Authorization başlığı gizlenir. Böylece backend
     * ve device access tokenları Logcat'e açık biçimde
     * yazılmaz.
     */
    private val httpLoggingInterceptor:
            HttpLoggingInterceptor by lazy {

        HttpLoggingInterceptor().apply {
            level =
                if (
                    BuildConfig.ENABLE_HTTP_LOGGING
                ) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }


            /*
             * Authorization başlığındaki Bearer tokenın
             * Logcat üzerinde görünmesini engeller.
             */
            redactHeader(
                "Authorization",
            )
        }
    }


    /*
     * =====================================================
     * ORTAK HTTP CLIENT
     * =====================================================
     */


    /**
     * Retrofit HTTP isteklerinde kullanılacak ortak
     * OkHttpClient nesnesidir.
     */
    private val httpClient:
            OkHttpClient by lazy {

        OkHttpClient.Builder()
            /*
             * Sunucu bağlantısının kurulması için
             * beklenecek maksimum süre.
             */
            .connectTimeout(
                20,
                TimeUnit.SECONDS,
            )

            /*
             * Sunucudan response okunması için
             * beklenecek maksimum süre.
             */
            .readTimeout(
                30,
                TimeUnit.SECONDS,
            )

            /*
             * Request body gönderilmesi için
             * beklenecek maksimum süre.
             */
            .writeTimeout(
                30,
                TimeUnit.SECONDS,
            )

            /*
             * Genel API çağrısının tamamlanması için
             * maksimum süre.
             */
            .callTimeout(
                45,
                TimeUnit.SECONDS,
            )

            .retryOnConnectionFailure(
                true,
            )

            .addInterceptor(
                httpLoggingInterceptor,
            )

            .build()
    }


    /*
     * =====================================================
     * WEBSOCKET CLIENT
     * =====================================================
     */


    /**
     * Authenticator WebSocket bağlantısında kullanılacak
     * ayrı OkHttpClient nesnesidir.
     *
     * WebSocket bağlantısı uzun süre açık kalacağı için
     * readTimeout değeri 0 olarak ayarlanır.
     *
     * pingInterval, OkHttp seviyesinde WebSocket ping
     * frameleri gönderir.
     *
     * Buna ek olarak AuthenticatorWebSocketManager
     * uygulama seviyesinde heartbeat mesajı da gönderir.
     */
    val webSocketClient:
            OkHttpClient by lazy {

        OkHttpClient.Builder()
            .connectTimeout(
                20,
                TimeUnit.SECONDS,
            )

            /*
             * WebSocket bağlantısı sürekli açık
             * kalacağından okuma zaman aşımı kapatılır.
             */
            .readTimeout(
                0,
                TimeUnit.MILLISECONDS,
            )

            .writeTimeout(
                20,
                TimeUnit.SECONDS,
            )

            /*
             * OkHttp seviyesinde her 25 saniyede bir
             * WebSocket ping frame gönderilir.
             */
            .pingInterval(
                25,
                TimeUnit.SECONDS,
            )

            .retryOnConnectionFailure(
                true,
            )

            .addInterceptor(
                httpLoggingInterceptor,
            )

            .build()
    }


    /*
     * =====================================================
     * .NET BACKEND RETROFIT
     * =====================================================
     */


    /**
     * Ana ProjectManagement .NET backend için Retrofit
     * nesnesidir.
     *
     * BuildConfig değeri:
     *
     * http://10.203.83.58:8080/
     */
    private val mainBackendRetrofit:
            Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(
                normalizeHttpBaseUrl(
                    BuildConfig.MAIN_BACKEND_BASE_URL,
                ),
            )

            .client(
                httpClient,
            )

            .addConverterFactory(
                GsonConverterFactory.create(
                    gson,
                ),
            )

            .build()
    }


    /**
     * .NET authentication endpointlerini kullanacak API
     * interface nesnesidir.
     */
    val mainBackendApi:
            MainBackendApi by lazy {

        mainBackendRetrofit.create(
            MainBackendApi::class.java,
        )
    }


    /*
     * =====================================================
     * PYTHON AUTHENTICATOR RETROFIT
     * =====================================================
     */


    /**
     * Python Authenticator servisi için ortak Retrofit
     * nesnesidir.
     *
     * BuildConfig değeri:
     *
     * http://10.203.83.58:8090/
     *
     * Devices ve challenge verification endpointleri
     * aynı Python servisi üzerinde çalıştığı için aynı
     * Retrofit nesnesi kullanılır.
     */
    private val authenticatorRetrofit:
            Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(
                normalizeHttpBaseUrl(
                    BuildConfig
                        .AUTHENTICATOR_BASE_URL,
                ),
            )

            .client(
                httpClient,
            )

            .addConverterFactory(
                GsonConverterFactory.create(
                    gson,
                ),
            )

            .build()
    }


    /**
     * Python servisindeki cihaz kayıt, heartbeat ve
     * cihaz bilgisi endpointlerini kullanır.
     */
    val authenticatorApi:
            AuthenticatorApi by lazy {

        authenticatorRetrofit.create(
            AuthenticatorApi::class.java,
        )
    }


    /**
     * Python servisindeki challenge onay ve ret
     * endpointini kullanır.
     *
     * Endpoint:
     *
     * POST
     * /api/challenges/{challenge_public_id}/decision
     */
    val challengeVerificationApi:
            ChallengeVerificationApi by lazy {

        authenticatorRetrofit.create(
            ChallengeVerificationApi::class.java,
        )
    }


    /*
     * =====================================================
     * WEBSOCKET URL
     * =====================================================
     */


    /**
     * BuildConfig içerisindeki Authenticator WebSocket
     * temel adresini döndürür.
     *
     * Beklenen değer:
     *
     * ws://10.203.83.58:8090
     *
     * Sonundaki slash kaldırılır. Böylece WebSocket
     * manager aşağıdaki endpointi güvenli biçimde
     * ekleyebilir:
     *
     * /ws/device
     */
    fun getAuthenticatorWebSocketBaseUrl():
            String {

        return normalizeWebSocketBaseUrl(
            BuildConfig
                .AUTHENTICATOR_WEBSOCKET_BASE_URL,
        )
    }


    /*
     * =====================================================
     * URL NORMALLEŞTİRME
     * =====================================================
     */


    /**
     * Retrofit base URL değerini normalize eder.
     *
     * Retrofit base URL mutlaka "/" ile bitmelidir.
     *
     * Örnek:
     *
     * http://10.203.83.58:8090
     *
     * şu hâle getirilir:
     *
     * http://10.203.83.58:8090/
     */
    private fun normalizeHttpBaseUrl(
        value: String,
    ): String {
        val normalizedValue =
            value.trim()


        require(
            normalizedValue.isNotBlank(),
        ) {
            "HTTP base URL boş olamaz."
        }


        require(
            normalizedValue.startsWith(
                prefix = "http://",
                ignoreCase = true,
            ) ||
                    normalizedValue.startsWith(
                        prefix = "https://",
                        ignoreCase = true,
                    ),
        ) {
            "HTTP base URL http:// veya https:// ile başlamalıdır."
        }


        return normalizedValue
            .trimEnd(
                '/',
            )
            .plus(
                "/",
            )
    }


    /**
     * WebSocket base URL değerini normalize eder.
     *
     * Örnek:
     *
     * ws://10.203.83.58:8090/
     *
     * şu hâle getirilir:
     *
     * ws://10.203.83.58:8090
     */
    private fun normalizeWebSocketBaseUrl(
        value: String,
    ): String {
        val normalizedValue =
            value.trim()


        require(
            normalizedValue.isNotBlank(),
        ) {
            "WebSocket base URL boş olamaz."
        }


        require(
            normalizedValue.startsWith(
                prefix = "ws://",
                ignoreCase = true,
            ) ||
                    normalizedValue.startsWith(
                        prefix = "wss://",
                        ignoreCase = true,
                    ),
        ) {
            "WebSocket URL ws:// veya wss:// ile başlamalıdır."
        }


        return normalizedValue.trimEnd(
            '/',
        )
    }
}