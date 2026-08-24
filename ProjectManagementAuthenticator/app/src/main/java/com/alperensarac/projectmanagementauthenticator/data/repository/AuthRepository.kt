package com.alperensarac.projectmanagementauthenticator.data.repository

import android.os.Build

import com.alperensarac.projectmanagementauthenticator.BuildConfig
import com.alperensarac.projectmanagementauthenticator.data.local.AuthSession
import com.alperensarac.projectmanagementauthenticator.data.local.AuthSessionManager
import com.alperensarac.projectmanagementauthenticator.data.remote.NetworkModule
import com.alperensarac.projectmanagementauthenticator.data.remote.api.AuthenticatorApi
import com.alperensarac.projectmanagementauthenticator.data.remote.api.MainBackendApi
import com.alperensarac.projectmanagementauthenticator.data.remote.model.DevicePlatform
import com.alperensarac.projectmanagementauthenticator.data.remote.model.DeviceRegistrationRequest
import com.alperensarac.projectmanagementauthenticator.data.remote.model.DeviceRegistrationResult
import com.alperensarac.projectmanagementauthenticator.data.remote.model.DeviceRegistrationValidationResult
import com.alperensarac.projectmanagementauthenticator.data.remote.model.FastApiErrorResponse
import com.alperensarac.projectmanagementauthenticator.data.remote.model.LoginRequest
import com.alperensarac.projectmanagementauthenticator.data.remote.model.LoginResult
import com.alperensarac.projectmanagementauthenticator.data.remote.model.LoginValidationResult
import com.alperensarac.projectmanagementauthenticator.security.DeviceKeyManager

import com.google.gson.JsonObject

import retrofit2.Response

import java.io.IOException
import java.util.Locale
import java.util.TimeZone


/*
 * =========================================================
 * AUTH REPOSITORY
 * =========================================================
 */


/**
 * Mobil Authenticator uygulamasındaki giriş ve cihaz
 * kayıt akışlarını yöneten Repository sınıfıdır.
 *
 * Bu sınıfın temel sorumlulukları:
 *
 * 1. Kullanıcıyı mevcut .NET backend üzerinde
 *    doğrulamak.
 *
 * 2. Başarılı login sonucundaki access token ve
 *    kullanıcı bilgilerini DataStore'a kaydetmek.
 *
 * 3. Android Keystore üzerinden cihaz public key
 *    bilgisini almak.
 *
 * 4. Mobil cihazı Python Authenticator servisine
 *    kaydetmek.
 *
 * 5. Python servisinden dönen device access tokenı
 *    DataStore'a kaydetmek.
 *
 * 6. Uygulama açılışında mevcut oturumu kontrol etmek.
 *
 * Repository doğrudan Activity veya View ile iletişim
 * kurmaz. Sonuçları ViewModel katmanına döndürür.
 */
class AuthRepository(
    private val mainBackendApi: MainBackendApi =
        NetworkModule.mainBackendApi,

    private val authenticatorApi: AuthenticatorApi =
        NetworkModule.authenticatorApi,

    private val authSessionManager: AuthSessionManager,

    private val deviceKeyManager: DeviceKeyManager =
        DeviceKeyManager(),
) {

    /*
     * =====================================================
     * .NET BACKEND LOGIN
     * =====================================================
     */


    /**
     * Kullanıcının mevcut ProjectManagement hesabıyla
     * giriş yapmasını sağlar.
     *
     * İşlem sırası:
     *
     * 1. E-posta ve şifre yerel olarak doğrulanır.
     * 2. POST /api/Auth/login endpointi çağrılır.
     * 3. Access token cevap içerisinden alınır.
     * 4. Gerekirse /api/Auth/me çağrısıyla kullanıcı
     *    bilgileri tamamlanır.
     * 5. Oturum DataStore'a kaydedilir.
     */
    suspend fun login(
        email: String,
        password: String,
    ): LoginResult {
        val loginRequest =
            LoginRequest(
                email = email,
                password = password,
            ).normalized()


        /*
         * Boş veya hatalı alanlarla gereksiz ağ isteği
         * göndermiyoruz.
         */
        when (
            val validationResult =
                loginRequest.validate()
        ) {
            is LoginValidationResult.Valid -> {
                // İstek gönderilebilir.
            }

            is LoginValidationResult.Invalid -> {
                return LoginResult.Failure(
                    message =
                    validationResult.message,
                )
            }
        }


        return try {
            val response =
                mainBackendApi.login(
                    request = loginRequest,
                )


            if (!response.isSuccessful) {
                return LoginResult.Failure(
                    message =
                    extractHttpErrorMessage(
                        response = response,
                        defaultMessage = (
                                "Giriş işlemi başarısız oldu."
                                ),
                    ),

                    httpStatusCode =
                    response.code(),
                )
            }


            val apiResponse =
                response.body()
                    ?: return LoginResult.Failure(
                        message = (
                                "Backend boş bir giriş "
                                        + "cevabı döndürdü."
                                ),

                        httpStatusCode =
                        response.code(),
                    )


            if (!apiResponse.success) {
                return LoginResult.Failure(
                    message =
                    apiResponse.getErrorMessage(
                        defaultMessage = (
                                "E-posta veya şifre "
                                        + "hatalı."
                                ),
                    ),

                    httpStatusCode =
                    response.code(),
                )
            }


            val loginData =
                apiResponse.data
                    ?: return LoginResult.Failure(
                        message = (
                                "Giriş cevabında token "
                                        + "bilgisi bulunamadı."
                                ),

                        httpStatusCode =
                        response.code(),
                    )


            val accessToken =
                loginData.resolveAccessToken()
                    ?: return LoginResult.Failure(
                        message = (
                                "Backend geçerli bir access "
                                        + "token döndürmedi."
                                ),

                        httpStatusCode =
                        response.code(),
                    )


            val refreshToken =
                loginData.resolveRefreshToken()


            /*
             * Bazı login endpointleri kullanıcı bilgilerini
             * doğrudan döndürmeyebilir.
             *
             * Önce login cevabındaki alanları kullanıyoruz.
             */
            var resolvedUserId =
                loginData.resolveUserId()

            var resolvedEmail =
                loginData.resolveEmail()
                    ?: loginRequest.email

            var resolvedDisplayName =
                loginData.resolveDisplayName()

            var resolvedRole =
                loginData.resolveRole()

            var resolvedIsActive =
                loginData.resolveIsActive()


            /*
             * Kullanıcı bilgileri eksikse access tokenla
             * /api/Auth/me endpointini çağırıyoruz.
             *
             * Bu çağrının başarısız olması login işlemini
             * tamamen bozmaz; access token geçerli şekilde
             * alınmışsa mevcut bilgilerle devam edilir.
             */
            val currentUserResult =
                getCurrentBackendUserSafely(
                    accessToken = accessToken,
                )


            if (currentUserResult != null) {
                resolvedUserId =
                    currentUserResult.id
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: resolvedUserId

                resolvedEmail =
                    currentUserResult.email
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: resolvedEmail

                resolvedDisplayName =
                    currentUserResult
                        .resolveDisplayName()
                        ?: resolvedDisplayName

                resolvedRole =
                    currentUserResult.resolveRole()
                        ?: resolvedRole

                resolvedIsActive =
                    currentUserResult.isActive
                        ?: resolvedIsActive
            }


            if (!resolvedIsActive) {
                return LoginResult.Failure(
                    message = (
                            "Kullanıcı hesabı aktif değil."
                            ),

                    httpStatusCode =
                    response.code(),
                )
            }


            /*
             * .NET backend oturumunu cihazda saklıyoruz.
             */
            authSessionManager.saveMainBackendSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                externalUserId = resolvedUserId,
                email = resolvedEmail,
                displayName = resolvedDisplayName,
            )


            LoginResult.Success(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = resolvedUserId,
                email = resolvedEmail,
                displayName = resolvedDisplayName,
                role = resolvedRole,
                isActive = resolvedIsActive,
            )
        } catch (exception: IOException) {
            LoginResult.Failure(
                message = (
                        "Ana backend servisine ulaşılamadı. "
                                + "Telefonun ve bilgisayarın aynı "
                                + "ağda olduğundan emin olun."
                        ),
            )
        } catch (exception: Exception) {
            LoginResult.Failure(
                message =
                exception.message
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: (
                            "Giriş işlemi sırasında "
                                    + "beklenmeyen bir hata "
                                    + "oluştu."
                            ),
            )
        }
    }


    /*
     * =====================================================
     * AUTHENTICATOR CİHAZ KAYDI
     * =====================================================
     */


    /**
     * Başarılı .NET login işleminden sonra Android
     * cihazını Python Authenticator servisine kaydeder.
     *
     * İşlem sırası:
     *
     * 1. DataStore'dan .NET access token alınır.
     * 2. Installation ID alınır veya oluşturulur.
     * 3. Android Keystore public key alınır.
     * 4. Cihaz ve işletim sistemi bilgileri hazırlanır.
     * 5. POST /api/devices/register çağrılır.
     * 6. Dönen device token ve public ID saklanır.
     */
    suspend fun registerCurrentDevice():
            DeviceRegistrationResult {

        val backendAccessToken =
            authSessionManager
                .getMainBackendAccessToken()
                ?: return DeviceRegistrationResult.Failure(
                    message = (
                            "Cihaz kaydı için önce kullanıcı "
                                    + "girişi yapılmalıdır."
                            ),
                )


        val installationId =
            try {
                authSessionManager
                    .getOrCreateInstallationId()
            } catch (exception: Exception) {
                return DeviceRegistrationResult.Failure(
                    message = (
                            "Installation ID oluşturulamadı: "
                                    + (
                                    exception.message
                                        ?: "Bilinmeyen hata."
                                    )
                            ),
                )
            }


        val publicKeyPem =
            try {
                deviceKeyManager
                    .getOrCreatePublicKeyPem()
            } catch (exception: Exception) {
                return DeviceRegistrationResult.Failure(
                    message = (
                            "Cihaz güvenlik anahtarı "
                                    + "oluşturulamadı: "
                                    + (
                                    exception.message
                                        ?: "Bilinmeyen hata."
                                    )
                            ),
                )
            }


        val registrationRequest =
            DeviceRegistrationRequest(
                backendAccessToken =
                backendAccessToken,

                installationId =
                installationId,

                platform =
                DevicePlatform.ANDROID.apiValue,

                deviceName =
                buildDeviceName(),

                deviceModel =
                Build.MODEL
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    },

                manufacturer =
                Build.MANUFACTURER
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    },

                osName =
                "Android",

                osVersion =
                Build.VERSION.RELEASE
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: Build.VERSION.SDK_INT
                        .toString(),

                appVersion =
                BuildConfig.VERSION_NAME,

                locale =
                resolveLocaleTag(),

                timezoneName =
                TimeZone
                    .getDefault()
                    .id,

                publicKey =
                publicKeyPem,

                publicKeyAlgorithm =
                "ECDSA_P256_SHA256",

                keyAttestation =
                null,

                pushToken =
                null,
            ).normalized()


        when (
            val validationResult =
                registrationRequest.validate()
        ) {
            is DeviceRegistrationValidationResult.Valid -> {
                // Request gönderilebilir.
            }

            is DeviceRegistrationValidationResult.Invalid -> {
                return DeviceRegistrationResult.Failure(
                    message =
                    validationResult.message,
                )
            }
        }


        return try {
            val response =
                authenticatorApi.registerDevice(
                    request = registrationRequest,
                )


            if (!response.isSuccessful) {
                return DeviceRegistrationResult.Failure(
                    message =
                    extractHttpErrorMessage(
                        response = response,
                        defaultMessage = (
                                "Authenticator cihaz "
                                        + "kaydı başarısız "
                                        + "oldu."
                                ),
                    ),

                    httpStatusCode =
                    response.code(),
                )
            }


            val apiResponse =
                response.body()
                    ?: return DeviceRegistrationResult.Failure(
                        message = (
                                "Authenticator servisi boş "
                                        + "bir cevap döndürdü."
                                ),

                        httpStatusCode =
                        response.code(),
                    )


            if (!apiResponse.success) {
                return DeviceRegistrationResult.Failure(
                    message =
                    apiResponse.getErrorMessage(
                        defaultMessage = (
                                "Cihaz kaydı "
                                        + "tamamlanamadı."
                                ),
                    ),

                    httpStatusCode =
                    response.code(),
                )
            }


            val registrationData =
                apiResponse.data
                    ?: return DeviceRegistrationResult.Failure(
                        message = (
                                "Cihaz kayıt cevabında data "
                                        + "alanı bulunamadı."
                                ),

                        httpStatusCode =
                        response.code(),
                    )


            val deviceAccessToken =
                registrationData
                    .deviceAccessToken
                    .trim()


            val devicePublicId =
                registrationData
                    .device
                    .publicId
                    .trim()


            if (deviceAccessToken.isBlank()) {
                return DeviceRegistrationResult.Failure(
                    message = (
                            "Cihaz kayıt cevabında device "
                                    + "access token bulunamadı."
                            ),

                    httpStatusCode =
                    response.code(),
                )
            }


            if (devicePublicId.isBlank()) {
                return DeviceRegistrationResult.Failure(
                    message = (
                            "Cihaz kayıt cevabında public ID "
                                    + "bulunamadı."
                            ),

                    httpStatusCode =
                    response.code(),
                )
            }


            /*
             * Python cihaz oturumunu DataStore'a
             * kaydediyoruz.
             */
            authSessionManager.saveDeviceSession(
                deviceAccessToken =
                deviceAccessToken,

                devicePublicId =
                devicePublicId,
            )


            DeviceRegistrationResult.Success(
                device =
                registrationData.device,

                deviceAccessToken =
                deviceAccessToken,

                expiresAt =
                registrationData.expiresAt,

                message =
                apiResponse.message,
            )
        } catch (exception: IOException) {
            DeviceRegistrationResult.Failure(
                message = (
                        "Python Authenticator servisine "
                                + "ulaşılamadı. Servisin 8090 "
                                + "portunda çalıştığını kontrol "
                                + "edin."
                        ),
            )
        } catch (exception: Exception) {
            DeviceRegistrationResult.Failure(
                message =
                exception.message
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: (
                            "Cihaz kaydı sırasında "
                                    + "beklenmeyen bir hata "
                                    + "oluştu."
                            ),
            )
        }
    }


    /*
     * =====================================================
     * BİRLEŞİK LOGIN VE CİHAZ KAYIT AKIŞI
     * =====================================================
     */


    /**
     * Mobil uygulamanın temel giriş akışını tek metotta
     * tamamlar.
     *
     * Önce .NET login işlemi yapılır, ardından cihaz
     * Python Authenticator servisine kaydedilir.
     *
     * Bu metot ileride LoginViewModel tarafından
     * çağrılacaktır.
     */
    suspend fun loginAndRegisterDevice(
        email: String,
        password: String,
    ): AuthenticatorLoginResult {
        val loginResult =
            login(
                email = email,
                password = password,
            )


        if (loginResult is LoginResult.Failure) {
            return AuthenticatorLoginResult.Failure(
                message =
                loginResult.message,

                stage =
                AuthenticatorLoginStage.LOGIN,

                httpStatusCode =
                loginResult.httpStatusCode,
            )
        }


        val successfulLogin =
            loginResult as LoginResult.Success


        val registrationResult =
            registerCurrentDevice()


        if (
            registrationResult
                    is DeviceRegistrationResult.Failure
        ) {
            /*
             * .NET login başarılı olsa bile cihaz kaydı
             * başarısız olabilir.
             *
             * Oturum bilgilerini silmiyoruz. Böylece
             * kullanıcı şifresini yeniden girmeden cihaz
             * kayıt işlemi tekrar denenebilir.
             */
            return AuthenticatorLoginResult.Failure(
                message =
                registrationResult.message,

                stage =
                AuthenticatorLoginStage
                    .DEVICE_REGISTRATION,

                httpStatusCode =
                registrationResult.httpStatusCode,

                loginSucceeded =
                true,
            )
        }


        val successfulRegistration =
            registrationResult
                    as DeviceRegistrationResult.Success


        return AuthenticatorLoginResult.Success(
            login =
            successfulLogin,

            deviceRegistration =
            successfulRegistration,
        )
    }


    /*
     * =====================================================
     * OTURUM OKUMA VE TEMİZLEME
     * =====================================================
     */


    /**
     * Güncel mobil oturumu döndürür.
     */
    suspend fun getCurrentSession():
            AuthSession {
        return authSessionManager
            .getCurrentSession()
    }


    /**
     * Kullanıcının .NET oturumu ve Python cihaz oturumu
     * bulunuyorsa tam Authenticator oturumu var kabul
     * edilir.
     */
    suspend fun hasCompleteAuthenticatorSession():
            Boolean {
        val session =
            getCurrentSession()

        return (
                session.isLoggedIn
                        && session.isDeviceRegistered
                        && !session
                    .mainBackendAccessToken
                    .isNullOrBlank()
                        && !session
                    .deviceAccessToken
                    .isNullOrBlank()
                        && !session
                    .devicePublicId
                    .isNullOrBlank()
                )
    }


    /**
     * Kullanıcı oturumunu temizler.
     *
     * Android Keystore anahtarı ve installation ID
     * korunur. Böylece kullanıcı yeniden giriş yaptığında
     * aynı cihaz kaydı güncellenebilir.
     */
    suspend fun logout() {
        authSessionManager.clearSession()
    }


    /**
     * Yalnızca Python cihaz oturumunu temizler.
     *
     * .NET login bilgisi korunur ve cihaz kaydı tekrar
     * denenebilir.
     */
    suspend fun clearDeviceSession() {
        authSessionManager.clearDeviceSession()
    }


    /**
     * Geliştirme amacıyla uygulamayı tamamen sıfırlar.
     *
     * Şunların tamamı silinir:
     *
     * - .NET oturumu
     * - Python cihaz oturumu
     * - Installation ID
     * - Android Keystore anahtarı
     *
     * Sonraki cihaz kaydında yeni installation ID ve
     * public/private key çifti oluşturulur.
     */
    suspend fun resetAuthenticatorInstallation() {
        authSessionManager.clearEverything()

        deviceKeyManager.deleteKeyPair()
    }


    /*
     * =====================================================
     * PRIVATE YARDIMCI METOTLAR
     * =====================================================
     */


    /**
     * /api/Auth/me endpointini güvenli biçimde çağırır.
     *
     * Endpoint başarısız olursa exception dışarı
     * aktarılmaz ve null döndürülür.
     */
    private suspend fun getCurrentBackendUserSafely(
        accessToken: String,
    ) = try {
        val authorizationHeader =
            "Bearer ${accessToken.trim()}"


        val response =
            mainBackendApi.getCurrentUser(
                authorizationHeader =
                authorizationHeader,
            )


        if (!response.isSuccessful) {
            null
        } else {
            val apiResponse =
                response.body()

            if (
                apiResponse?.success == true
            ) {
                apiResponse.data
            } else {
                null
            }
        }
    } catch (_: Exception) {
        null
    }


    /**
     * Android cihazı için kullanıcıya gösterilebilir bir
     * isim üretir.
     *
     * Örnek:
     *
     * Samsung SM-S918B
     */
    private fun buildDeviceName(): String {
        val manufacturer =
            Build.MANUFACTURER
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val model =
            Build.MODEL
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }


        return listOfNotNull(
            manufacturer,
            model,
        )
            .distinct()
            .joinToString(
                separator = " ",
            )
            .takeIf {
                it.isNotBlank()
            }
            ?: "Android Authenticator"
    }


    /**
     * Cihazın dil ve bölge bilgisini BCP 47 formatında
     * döndürür.
     *
     * Örnek:
     *
     * tr-TR
     */
    private fun resolveLocaleTag(): String {
        return Locale
            .getDefault()
            .toLanguageTag()
            .takeIf {
                it.isNotBlank()
            }
            ?: Locale
                .getDefault()
                .toString()
    }


    /**
     * Başarısız HTTP cevabındaki hata gövdesini okunabilir
     * metne dönüştürür.
     *
     * Hem FastAPI:
     *
     * {
     *   "detail": "Hata mesajı"
     * }
     *
     * hem de ortak ApiResponse:
     *
     * {
     *   "success": false,
     *   "message": "Hata mesajı",
     *   "errors": {}
     * }
     *
     * yapıları desteklenir.
     */
    private fun <T> extractHttpErrorMessage(
        response: Response<T>,
        defaultMessage: String,
    ): String {
        val errorBody =
            try {
                response
                    .errorBody()
                    ?.string()
                    ?.trim()
            } catch (_: Exception) {
                null
            }


        if (errorBody.isNullOrBlank()) {
            return buildHttpDefaultMessage(
                statusCode = response.code(),
                defaultMessage = defaultMessage,
            )
        }


        /*
         * Önce FastAPI detail yapısını okumayı deniyoruz.
         */
        try {
            val fastApiError =
                NetworkModule.gson.fromJson(
                    errorBody,
                    FastApiErrorResponse::class.java,
                )

            val message =
                fastApiError.getErrorMessage(
                    defaultMessage = "",
                )

            if (message.isNotBlank()) {
                return message
            }
        } catch (_: Exception) {
            // Diğer hata formatlarını denemeye devam et.
        }


        /*
         * Ardından genel JSON alanlarını manuel okuyoruz.
         *
         * Generic ApiResponse tipini Gson ile doğrudan
         * çözmek yerine hata mesajı için gerekli alanları
         * JsonObject üzerinden okuyoruz.
         */
        try {
            val jsonObject =
                NetworkModule.gson.fromJson(
                    errorBody,
                    JsonObject::class.java,
                )


            val message =
                jsonObject
                    ?.get("message")
                    ?.takeIf {
                        !it.isJsonNull
                    }
                    ?.asString
                    ?.trim()


            if (!message.isNullOrBlank()) {
                return message
            }


            val title =
                jsonObject
                    ?.get("title")
                    ?.takeIf {
                        !it.isJsonNull
                    }
                    ?.asString
                    ?.trim()


            if (!title.isNullOrBlank()) {
                return title
            }
        } catch (_: Exception) {
            // Ham response metnine geç.
        }


        /*
         * JSON çözümlenemiyorsa çok uzun olmayan ham
         * response metnini gösteriyoruz.
         */
        if (errorBody.length <= 500) {
            return errorBody
        }


        return buildHttpDefaultMessage(
            statusCode = response.code(),
            defaultMessage = defaultMessage,
        )
    }


    /**
     * HTTP durum koduna göre daha açıklayıcı varsayılan
     * hata mesajı üretir.
     */
    private fun buildHttpDefaultMessage(
        statusCode: Int,
        defaultMessage: String,
    ): String {
        return when (statusCode) {
            400 -> {
                "Gönderilen bilgiler geçersiz."
            }

            401 -> {
                "Oturum bilgileri geçersiz veya süresi dolmuş."
            }

            403 -> {
                "Bu işlem için yetkiniz bulunmuyor."
            }

            404 -> {
                "İstenen servis veya kayıt bulunamadı."
            }

            409 -> {
                "İşlem mevcut kayıtla çakıştı."
            }

            422 -> {
                "Gönderilen alanlardan biri doğrulanamadı."
            }

            in 500..599 -> {
                "Sunucu tarafında bir hata oluştu."
            }

            else -> {
                defaultMessage
            }
        }
    }
}


/*
 * =========================================================
 * BİRLEŞİK AUTHENTICATOR LOGIN SONUCU
 * =========================================================
 */


/**
 * Login ve cihaz kayıt akışının hangi aşamada olduğunu
 * belirtir.
 */
enum class AuthenticatorLoginStage {
    /**
     * .NET backend login aşaması.
     */
    LOGIN,

    /**
     * Python Authenticator cihaz kayıt aşaması.
     */
    DEVICE_REGISTRATION,
}


/**
 * Login ve cihaz kayıt işlemlerinin birleşik sonucudur.
 */
sealed interface AuthenticatorLoginResult {

    /**
     * Hem .NET login hem Python cihaz kaydı başarılıdır.
     */
    data class Success(
        val login: LoginResult.Success,

        val deviceRegistration:
        DeviceRegistrationResult.Success,
    ) : AuthenticatorLoginResult


    /**
     * Akışın herhangi bir aşamasında hata oluşmuştur.
     */
    data class Failure(
        val message: String,

        val stage: AuthenticatorLoginStage,

        val httpStatusCode: Int? = null,

        /**
         * .NET login başarılı fakat cihaz kaydı başarısız
         * olduğunda true olur.
         *
         * Böylece ViewModel kullanıcıya yalnızca cihaz
         * kaydını tekrar deneme seçeneği sunabilir.
         */
        val loginSucceeded: Boolean = false,
    ) : AuthenticatorLoginResult
}