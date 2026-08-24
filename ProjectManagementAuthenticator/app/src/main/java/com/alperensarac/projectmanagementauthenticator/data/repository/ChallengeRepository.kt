package com.alperensarac.projectmanagementauthenticator.data.repository

import com.alperensarac.projectmanagementauthenticator.data.local.AuthSessionManager
import com.alperensarac.projectmanagementauthenticator.data.remote.NetworkModule
import com.alperensarac.projectmanagementauthenticator.data.remote.api.ChallengeVerificationApi
import com.alperensarac.projectmanagementauthenticator.data.remote.model.ChallengeDecision
import com.alperensarac.projectmanagementauthenticator.data.remote.model.ChallengeDecisionRequest
import com.alperensarac.projectmanagementauthenticator.data.remote.model.ChallengeDecisionResult
import com.alperensarac.projectmanagementauthenticator.data.remote.model.ChallengeDecisionValidationResult
import com.alperensarac.projectmanagementauthenticator.data.remote.model.LocationPermissionStatus
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketChallengeMessage
import com.alperensarac.projectmanagementauthenticator.security.ChallengeSigningPayloadBuilder
import com.alperensarac.projectmanagementauthenticator.security.DeviceKeyManager

import com.google.gson.JsonObject
import com.google.gson.JsonParser

import retrofit2.Response

import java.io.IOException


/*
 * =========================================================
 * CHALLENGE REPOSITORY
 * =========================================================
 */


/**
 * WebSocket üzerinden alınan authentication challenge
 * mesajlarının onaylanması veya reddedilmesi işlemlerini
 * yöneten Repository sınıfıdır.
 *
 * Bu sınıfın sorumlulukları:
 *
 * 1. DataStore üzerinden cihaz oturumunu okumak.
 * 2. Challenge için Python ile uyumlu imza payloadı
 *    oluşturmak.
 * 3. Payloadı Android Keystore private key'iyle
 *    imzalamak.
 * 4. Onay veya ret kararını Python servisine göndermek.
 * 5. HTTP ve doğrulama hatalarını sade sonuç modeline
 *    dönüştürmek.
 *
 * Activity ve ViewModel doğrudan:
 *
 * - Android Keystore,
 * - Retrofit,
 * - DataStore
 *
 * ile iletişim kurmaz.
 */
class ChallengeRepository(
    private val authSessionManager: AuthSessionManager,

    private val challengeVerificationApi:
    ChallengeVerificationApi =
        NetworkModule.challengeVerificationApi,

    private val deviceKeyManager: DeviceKeyManager =
        DeviceKeyManager(),

    private val signingPayloadBuilder:
    ChallengeSigningPayloadBuilder =
        ChallengeSigningPayloadBuilder(),
) {

    /*
     * =====================================================
     * PUBLIC KARAR METOTLARI
     * =====================================================
     */


    /**
     * Challenge mesajını onaylar.
     *
     * Konum bilgisi kullanılmayacaksa yalnızca challenge
     * parametresinin verilmesi yeterlidir.
     */
    suspend fun approveChallenge(
        challenge: WebSocketChallengeMessage,
        location: ChallengeLocationData? = null,
    ): ChallengeDecisionResult {
        return sendChallengeDecision(
            challenge = challenge,
            decision = ChallengeDecision.APPROVE,
            location = location,
        )
    }


    /**
     * Challenge mesajını reddeder.
     */
    suspend fun rejectChallenge(
        challenge: WebSocketChallengeMessage,
        location: ChallengeLocationData? = null,
    ): ChallengeDecisionResult {
        return sendChallengeDecision(
            challenge = challenge,
            decision = ChallengeDecision.REJECT,
            location = location,
        )
    }


    /**
     * Challenge için onay veya ret kararını Python
     * Authenticator servisine gönderir.
     *
     * İşlem sırası:
     *
     * 1. Challenge mesajı doğrulanır.
     * 2. Cihaz access tokenı okunur.
     * 3. Installation ID okunur.
     * 4. İmzalama payloadı oluşturulur.
     * 5. Android Keystore private key'iyle imzalanır.
     * 6. Device token ile HTTP isteği gönderilir.
     */
    suspend fun sendChallengeDecision(
        challenge: WebSocketChallengeMessage,
        decision: ChallengeDecision,
        location: ChallengeLocationData? = null,
    ): ChallengeDecisionResult {

        /*
         * WebSocket mesajının gerekli alanlarını tekrar
         * kontrol ediyoruz.
         *
         * WebSocketManager içinde de kontrol yapılır;
         * ancak Repository tek başına kullanıldığında da
         * güvenli davranmalıdır.
         */
        val challengeValidation =
            challenge.validate()

        if (
            challengeValidation
                    is com.alperensarac
            .projectmanagementauthenticator
            .data.remote.model
            .WebSocketMessageValidationResult.Invalid
        ) {
            return ChallengeDecisionResult.Failure(
                message =
                challengeValidation.message,
            )
        }


        /*
         * Cihaz oturum bilgilerini DataStore üzerinden
         * okuyoruz.
         */
        val session =
            try {
                authSessionManager
                    .getCurrentSession()
            } catch (exception: Exception) {
                return ChallengeDecisionResult.Failure(
                    message = (
                            "Cihaz oturum bilgileri "
                                    + "okunamadı: "
                                    + (
                                    exception.message
                                        ?: "Bilinmeyen hata."
                                    )
                            ),
                )
            }


        val deviceAccessToken =
            session.deviceAccessToken
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return ChallengeDecisionResult.Failure(
                    message = (
                            "Challenge kararı göndermek için "
                                    + "cihaz access tokenı "
                                    + "bulunamadı."
                            ),
                )


        val installationId =
            session.installationId
                .trim()
                .takeIf {
                    it.isNotBlank()
                }
                ?: return ChallengeDecisionResult.Failure(
                    message = (
                            "Challenge kararı göndermek için "
                                    + "installation ID bulunamadı."
                            ),
                )


        /*
         * Challenge'ın gönderildiği cihaz ile mevcut
         * oturumdaki cihazı karşılaştırıyoruz.
         *
         * Farklı cihaz için gelen challenge yanlışlıkla
         * onaylanmamalıdır.
         */
        val storedDevicePublicId =
            session.devicePublicId
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }


        if (
            storedDevicePublicId != null &&
            !challenge.devicePublicId.equals(
                other = storedDevicePublicId,
                ignoreCase = true,
            )
        ) {
            return ChallengeDecisionResult.Failure(
                message = (
                        "Doğrulama isteği bu cihaza ait "
                                + "değil."
                        ),
            )
        }


        /*
         * Python security.py içerisindeki
         * build_challenge_signing_payload fonksiyonuyla
         * aynı byte dizisini oluşturuyoruz.
         */
        val payloadBytes =
            try {
                signingPayloadBuilder
                    .buildPayloadBytes(
                        challengePublicId =
                        challenge.challengePublicId,

                        nonce =
                        challenge.nonce,

                        externalUserId =
                        challenge.externalUserId,

                        installationId =
                        installationId,

                        decision =
                        decision,

                        expiresAt =
                        challenge.expiresAt,
                    )
            } catch (exception: Exception) {
                return ChallengeDecisionResult.Failure(
                    message = (
                            "Challenge imzalama metni "
                                    + "oluşturulamadı: "
                                    + (
                                    exception.message
                                        ?: "Bilinmeyen hata."
                                    )
                            ),
                )
            }


        /*
         * Payload Android Keystore içerisindeki private
         * key ile SHA256withECDSA kullanılarak imzalanır.
         *
         * Private key hiçbir zaman uygulama dışına
         * çıkarılmaz.
         */
        val signatureBase64 =
            try {
                deviceKeyManager.signPayload(
                    payload =
                    payloadBytes,
                )
            } catch (exception: Exception) {
                return ChallengeDecisionResult.Failure(
                    message = (
                            "Challenge cihaz anahtarıyla "
                                    + "imzalanamadı: "
                                    + (
                                    exception.message
                                        ?: "Bilinmeyen hata."
                                    )
                            ),
                )
            }


        val normalizedLocation =
            location?.normalized()


        val request =
            ChallengeDecisionRequest(
                decision =
                decision.apiValue,

                installationId =
                installationId,

                signature =
                signatureBase64,

                latitude =
                normalizedLocation?.latitude,

                longitude =
                normalizedLocation?.longitude,

                locationAccuracyMeters =
                normalizedLocation
                    ?.accuracyMeters,

                locationPermissionStatus =
                normalizedLocation
                    ?.permissionStatus
                    ?.apiValue
                    ?: LocationPermissionStatus
                        .NOT_REQUESTED
                        .apiValue,

                locationCapturedAt =
                normalizedLocation
                    ?.capturedAt,
            ).normalized()


        /*
         * API'ye göndermeden önce request modelinin
         * temel doğrulamasını yapıyoruz.
         */
        when (
            val requestValidation =
                request.validate()
        ) {
            ChallengeDecisionValidationResult.Valid -> {
                // İstek gönderilebilir.
            }

            is ChallengeDecisionValidationResult.Invalid -> {
                return ChallengeDecisionResult.Failure(
                    message =
                    requestValidation.message,
                )
            }
        }


        return performDecisionRequest(
            challengePublicId =
            challenge.challengePublicId,

            deviceAccessToken =
            deviceAccessToken,

            request =
            request,
        )
    }


    /*
     * =====================================================
     * HTTP İSTEĞİ
     * =====================================================
     */


    /**
     * Hazırlanmış challenge karar requestini Python
     * servisine gönderir.
     */
    private suspend fun performDecisionRequest(
        challengePublicId: String,
        deviceAccessToken: String,
        request: ChallengeDecisionRequest,
    ): ChallengeDecisionResult {
        return try {
            val response =
                challengeVerificationApi
                    .sendChallengeDecision(
                        authorizationHeader =
                        createBearerToken(
                            token =
                            deviceAccessToken,
                        ),

                        challengePublicId =
                        challengePublicId.trim(),

                        request =
                        request,
                    )


            if (!response.isSuccessful) {
                return ChallengeDecisionResult.Failure(
                    message =
                    extractHttpErrorMessage(
                        response = response,
                        defaultMessage = (
                                "Challenge kararı "
                                        + "gönderilemedi."
                                ),
                    ),

                    httpStatusCode =
                    response.code(),
                )
            }


            val responseBody =
                response.body()
                    ?: return ChallengeDecisionResult.Failure(
                        message = (
                                "Python servisi boş bir "
                                        + "challenge cevabı "
                                        + "döndürdü."
                                ),

                        httpStatusCode =
                        response.code(),
                    )


            if (!responseBody.success) {
                return ChallengeDecisionResult.Failure(
                    message =
                    responseBody.getErrorMessage(
                        defaultMessage = (
                                "Challenge kararı "
                                        + "işlenemedi."
                                ),
                    ),

                    httpStatusCode =
                    response.code(),
                )
            }


            val verificationData =
                responseBody.data
                    ?: return ChallengeDecisionResult.Failure(
                        message = (
                                "Challenge cevabında "
                                        + "doğrulama sonucu "
                                        + "bulunamadı."
                                ),

                        httpStatusCode =
                        response.code(),
                    )


            ChallengeDecisionResult.Success(
                verification =
                verificationData,

                message =
                responseBody.message,
            )
        } catch (exception: IOException) {
            ChallengeDecisionResult.Failure(
                message = (
                        "Python Authenticator servisine "
                                + "ulaşılamadı. Ağ bağlantısını "
                                + "ve 8090 portunu kontrol edin."
                        ),
            )
        } catch (exception: Exception) {
            ChallengeDecisionResult.Failure(
                message =
                exception.message
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: (
                            "Challenge kararı sırasında "
                                    + "beklenmeyen bir hata "
                                    + "oluştu."
                            ),
            )
        }
    }


    /*
     * =====================================================
     * YARDIMCI METOTLAR
     * =====================================================
     */


    /**
     * Ham tokenı HTTP Authorization başlığında
     * kullanılabilecek biçime dönüştürür.
     */
    private fun createBearerToken(
        token: String,
    ): String {
        val normalizedToken =
            token.trim()

        return if (
            normalizedToken.startsWith(
                prefix = "Bearer ",
                ignoreCase = true,
            )
        ) {
            normalizedToken
        } else {
            "Bearer $normalizedToken"
        }
    }


    /**
     * Başarısız HTTP cevabındaki FastAPI hata mesajını
     * kullanıcıya gösterilebilir metne dönüştürür.
     *
     * FastAPI hataları çoğunlukla şu yapıdadır:
     *
     * {
     *   "detail": "Challenge süresi dolmuş."
     * }
     */
    private fun <T> extractHttpErrorMessage(
        response: Response<T>,
        defaultMessage: String,
    ): String {
        val rawErrorBody =
            try {
                response
                    .errorBody()
                    ?.string()
                    ?.trim()
            } catch (_: Exception) {
                null
            }


        if (rawErrorBody.isNullOrBlank()) {
            return createDefaultHttpMessage(
                statusCode =
                response.code(),

                defaultMessage =
                defaultMessage,
            )
        }


        try {
            val jsonElement =
                JsonParser.parseString(
                    rawErrorBody,
                )


            if (jsonElement.isJsonObject) {
                val jsonObject =
                    jsonElement.asJsonObject


                /*
                 * FastAPI standart HTTPException cevabı.
                 */
                val detail =
                    readJsonErrorValue(
                        jsonObject =
                        jsonObject,

                        key =
                        "detail",
                    )


                if (!detail.isNullOrBlank()) {
                    return detail
                }


                /*
                 * Ortak ApiResponse hata mesajı.
                 */
                val message =
                    readJsonErrorValue(
                        jsonObject =
                        jsonObject,

                        key =
                        "message",
                    )


                if (!message.isNullOrBlank()) {
                    return message
                }


                /*
                 * ASP.NET ProblemDetails benzeri cevaplar
                 * için title alanı da desteklenir.
                 */
                val title =
                    readJsonErrorValue(
                        jsonObject =
                        jsonObject,

                        key =
                        "title",
                    )


                if (!title.isNullOrBlank()) {
                    return title
                }
            }
        } catch (_: Exception) {
            /*
             * JSON parse edilemezse aşağıdaki ham metin
             * veya varsayılan mesaj kullanılacaktır.
             */
        }


        if (rawErrorBody.length <= 500) {
            return rawErrorBody
        }


        return createDefaultHttpMessage(
            statusCode =
            response.code(),

            defaultMessage =
            defaultMessage,
        )
    }


    /**
     * JSON hata alanını String, liste veya nesne
     * biçimlerinden okunabilir metne dönüştürür.
     */
    private fun readJsonErrorValue(
        jsonObject: JsonObject,
        key: String,
    ): String? {
        val element =
            jsonObject.get(
                key,
            )
                ?: return null


        if (element.isJsonNull) {
            return null
        }


        return try {
            when {
                element.isJsonPrimitive -> {
                    element
                        .asString
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        }
                }

                element.isJsonArray -> {
                    element
                        .asJsonArray
                        .mapNotNull {
                                item ->

                            try {
                                item
                                    .asString
                                    .trim()
                                    .takeIf {
                                        it.isNotBlank()
                                    }
                            } catch (_: Exception) {
                                null
                            }
                        }
                        .joinToString(
                            separator = "\n",
                        )
                        .takeIf {
                            it.isNotBlank()
                        }
                }

                else -> {
                    element
                        .toString()
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        }
                }
            }
        } catch (_: Exception) {
            null
        }
    }


    /**
     * HTTP durum koduna göre sade hata mesajı üretir.
     */
    private fun createDefaultHttpMessage(
        statusCode: Int,
        defaultMessage: String,
    ): String {
        return when (statusCode) {
            400 -> {
                "Challenge kararı geçersiz."
            }

            401 -> {
                "Cihaz oturumunun süresi dolmuş veya token geçersiz."
            }

            403 -> {
                "Challenge bu cihazla eşleşmiyor."
            }

            404 -> {
                "Doğrulama isteği bulunamadı."
            }

            409 -> {
                "Doğrulama isteği daha önce tamamlanmış."
            }

            410 -> {
                "Doğrulama isteğinin süresi dolmuş."
            }

            422 -> {
                "Gönderilen challenge bilgileri doğrulanamadı."
            }

            423 -> {
                "Doğrulama isteği kilitlenmiş."
            }

            in 500..599 -> {
                "Authenticator sunucusunda bir hata oluştu."
            }

            else -> {
                defaultMessage
            }
        }
    }
}


/*
 * =========================================================
 * CHALLENGE KONUM MODELİ
 * =========================================================
 */


/**
 * Challenge kararı sırasında isteğe bağlı olarak
 * gönderilebilecek cihaz konumunu temsil eder.
 *
 * Bu model doğrudan Retrofit request modeli değildir.
 * Repository tarafından ChallengeDecisionRequest
 * modeline dönüştürülür.
 */
data class ChallengeLocationData(
    /**
     * Enlem değeri.
     */
    val latitude: Double? = null,

    /**
     * Boylam değeri.
     */
    val longitude: Double? = null,

    /**
     * Konum doğruluğu, metre cinsinden.
     */
    val accuracyMeters: Double? = null,

    /**
     * Android konum izninin durumu.
     */
    val permissionStatus:
    LocationPermissionStatus =
        LocationPermissionStatus.NOT_REQUESTED,

    /**
     * Konumun alındığı UTC zaman.
     *
     * ISO-8601 örneği:
     *
     * 2026-08-03T10:20:00Z
     */
    val capturedAt: String? = null,
) {
    /**
     * Konum değerlerini güvenli aralıklara göre
     * normalleştirir.
     *
     * Geçersiz koordinatlar null yapılır.
     */
    fun normalized(): ChallengeLocationData {
        val normalizedLatitude =
            latitude?.takeIf {
                it in -90.0..90.0
            }

        val normalizedLongitude =
            longitude?.takeIf {
                it in -180.0..180.0
            }

        val normalizedAccuracy =
            accuracyMeters?.takeIf {
                it in 0.0..100_000.0
            }

        val normalizedCapturedAt =
            capturedAt
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }


        /*
         * Enlem veya boylamdan yalnızca biri varsa eksik
         * konum bilgisini göndermiyoruz.
         */
        val hasCompleteCoordinate =
            normalizedLatitude != null &&
                    normalizedLongitude != null


        return if (hasCompleteCoordinate) {
            copy(
                latitude =
                normalizedLatitude,

                longitude =
                normalizedLongitude,

                accuracyMeters =
                normalizedAccuracy,

                capturedAt =
                normalizedCapturedAt,
            )
        } else {
            copy(
                latitude =
                null,

                longitude =
                null,

                accuracyMeters =
                null,

                capturedAt =
                null,
            )
        }
    }
}