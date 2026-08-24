package com.alperensarac.projectmanagementauthenticator.data.remote.model

import com.google.gson.annotations.SerializedName


/*
 * =========================================================
 * CHALLENGE KARAR TİPLERİ
 * =========================================================
 */


/**
 * Mobil Authenticator uygulamasının bir challenge için
 * verebileceği kararları temsil eder.
 *
 * Python servisinin kabul ettiği değerler:
 *
 * - approve
 * - reject
 */
enum class ChallengeDecision(
    val apiValue: String,
) {
    /**
     * Kullanıcı giriş isteğini onayladı.
     */
    APPROVE(
        apiValue = "approve",
    ),

    /**
     * Kullanıcı giriş isteğini reddetti.
     */
    REJECT(
        apiValue = "reject",
    ),
}


/*
 * =========================================================
 * KONUM İZİN DURUMU
 * =========================================================
 */


/**
 * Mobil cihazın konum izni durumunu temsil eder.
 *
 * Bu değerler Python tarafındaki
 * ChallengeDecisionRequest modeliyle birebir uyumludur.
 */
enum class LocationPermissionStatus(
    val apiValue: String,
) {
    /**
     * Kullanıcı hassas konum izni verdi.
     */
    GRANTED_PRECISE(
        apiValue = "granted_precise",
    ),

    /**
     * Kullanıcı yaklaşık konum izni verdi.
     */
    GRANTED_APPROXIMATE(
        apiValue = "granted_approximate",
    ),

    /**
     * Kullanıcı konum iznini reddetti.
     */
    DENIED(
        apiValue = "denied",
    ),

    /**
     * Konum erişimi sistem veya cihaz politikası
     * nedeniyle sınırlandırılmış.
     */
    RESTRICTED(
        apiValue = "restricted",
    ),

    /**
     * Konum izni henüz kullanıcıdan istenmedi.
     */
    NOT_REQUESTED(
        apiValue = "not_requested",
    ),

    /**
     * Konum özelliği cihazda kullanılamıyor.
     */
    UNAVAILABLE(
        apiValue = "unavailable",
    ),
}


/*
 * =========================================================
 * CHALLENGE KARAR REQUEST MODELİ
 * =========================================================
 */


/**
 * Mobil Authenticator uygulamasının challenge için
 * verdiği onay veya ret kararını Python servisine
 * gönderen request modelidir.
 *
 * Python tarafındaki beklenen JSON:
 *
 * {
 *   "decision": "approve",
 *   "installation_id": "...",
 *   "signature": "...",
 *   "latitude": 41.0082,
 *   "longitude": 28.9784,
 *   "location_accuracy_meters": 10.5,
 *   "location_permission_status": "granted_precise",
 *   "location_captured_at": "2026-08-03T10:15:00Z"
 * }
 *
 * Private key hiçbir zaman request içerisinde
 * gönderilmez.
 *
 * Yalnızca challenge bilgileriyle oluşturulan payloadın
 * Android Keystore private key'iyle üretilmiş Base64
 * imzası gönderilir.
 */
data class ChallengeDecisionRequest(
    /**
     * Kullanıcının verdiği karar.
     *
     * Python servisi yalnızca approve veya reject
     * değerlerini kabul eder.
     */
    @SerializedName("decision")
    val decision: String,

    /**
     * Uygulamanın mevcut kurulum kimliği.
     *
     * Device access token içerisindeki installation ID
     * ve veritabanındaki cihaz kaydıyla aynı olmalıdır.
     */
    @SerializedName("installation_id")
    val installationId: String,

    /**
     * Challenge karar payloadının Android Keystore
     * private key'iyle üretilmiş Base64 imzasıdır.
     */
    @SerializedName("signature")
    val signature: String,

    /**
     * Mobil cihazın karar anındaki enlem bilgisi.
     *
     * Konum alınamadığında null olabilir.
     */
    @SerializedName("latitude")
    val latitude: Double? = null,

    /**
     * Mobil cihazın karar anındaki boylam bilgisi.
     *
     * Konum alınamadığında null olabilir.
     */
    @SerializedName("longitude")
    val longitude: Double? = null,

    /**
     * Konum doğruluğunu metre cinsinden belirtir.
     */
    @SerializedName("location_accuracy_meters")
    val locationAccuracyMeters: Double? = null,

    /**
     * Mobil cihazdaki konum izninin durumudur.
     */
    @SerializedName("location_permission_status")
    val locationPermissionStatus: String? = null,

    /**
     * Konum bilgisinin cihaz tarafından alındığı UTC
     * zamanıdır.
     *
     * ISO 8601 biçiminde gönderilir.
     */
    @SerializedName("location_captured_at")
    val locationCapturedAt: String? = null,
) {
    /**
     * Request içindeki metin alanlarını temizlenmiş
     * biçimde döndürür.
     */
    fun normalized(): ChallengeDecisionRequest {
        return copy(
            decision =
            decision.trim().lowercase(),

            installationId =
            installationId.trim(),

            signature =
            signature.trim(),

            locationPermissionStatus =
            locationPermissionStatus
                .normalizeNullable(),

            locationCapturedAt =
            locationCapturedAt
                .normalizeNullable(),
        )
    }


    /**
     * Request alanlarını API isteği gönderilmeden önce
     * yerel olarak kontrol eder.
     *
     * Bu kontroller Python tarafındaki Pydantic
     * doğrulamasının yerine geçmez.
     */
    fun validate(): ChallengeDecisionValidationResult {
        val normalizedRequest =
            normalized()


        if (
            normalizedRequest.decision !in setOf(
                ChallengeDecision.APPROVE.apiValue,
                ChallengeDecision.REJECT.apiValue,
            )
        ) {
            return ChallengeDecisionValidationResult.Invalid(
                message = (
                        "Challenge kararı approve veya "
                                + "reject olmalıdır."
                        ),
            )
        }


        if (
            normalizedRequest.installationId.length
            < 16
        ) {
            return ChallengeDecisionValidationResult.Invalid(
                message = (
                        "Installation ID en az 16 karakter "
                                + "olmalıdır."
                        ),
            )
        }


        if (
            normalizedRequest.signature.length
            < 20
        ) {
            return ChallengeDecisionValidationResult.Invalid(
                message = (
                        "Challenge imzası geçerli değil."
                        ),
            )
        }


        if (
            normalizedRequest.latitude != null &&
            normalizedRequest.latitude !in -90.0..90.0
        ) {
            return ChallengeDecisionValidationResult.Invalid(
                message = (
                        "Enlem değeri -90 ile 90 arasında "
                                + "olmalıdır."
                        ),
            )
        }


        if (
            normalizedRequest.longitude != null &&
            normalizedRequest.longitude !in -180.0..180.0
        ) {
            return ChallengeDecisionValidationResult.Invalid(
                message = (
                        "Boylam değeri -180 ile 180 arasında "
                                + "olmalıdır."
                        ),
            )
        }


        if (
            normalizedRequest.locationAccuracyMeters != null &&
            (
                    normalizedRequest.locationAccuracyMeters < 0.0 ||
                            normalizedRequest.locationAccuracyMeters > 100_000.0
                    )
        ) {
            return ChallengeDecisionValidationResult.Invalid(
                message = (
                        "Konum doğruluk değeri 0 ile 100000 "
                                + "metre arasında olmalıdır."
                        ),
            )
        }


        val permissionStatus =
            normalizedRequest
                .locationPermissionStatus


        if (
            permissionStatus != null &&
            permissionStatus !in
            LocationPermissionStatus.entries.map {
                    status ->

                status.apiValue
            }
        ) {
            return ChallengeDecisionValidationResult.Invalid(
                message = (
                        "Konum izin durumu desteklenmiyor."
                        ),
            )
        }


        return ChallengeDecisionValidationResult.Valid
    }
}


/**
 * Challenge karar requestinin yerel doğrulama sonucudur.
 */
sealed interface ChallengeDecisionValidationResult {

    /**
     * Request temel kontrollerden geçti.
     */
    data object Valid :
        ChallengeDecisionValidationResult

    /**
     * Request içerisindeki alanlardan biri geçersiz.
     */
    data class Invalid(
        val message: String,
    ) : ChallengeDecisionValidationResult
}


/*
 * =========================================================
 * CHALLENGE DOĞRULAMA SONUCU
 * =========================================================
 */


/**
 * Python servisindeki ChallengeVerificationResponse
 * modelini temsil eder.
 *
 * Hem kod doğrulama hem mobil cihaz kararı sonucunda
 * kullanılabilecek ortak response modelidir.
 */
data class ChallengeVerificationData(
    /**
     * İşlem yapılan challenge public ID değeri.
     */
    @SerializedName("challenge_public_id")
    val challengePublicId: String,

    /**
     * Challenge'ın işlem sonrasındaki durumu.
     *
     * Örnek:
     *
     * approved
     * rejected
     * locked
     */
    @SerializedName("status")
    val status: String,

    /**
     * AuthenticationAttempt sonucudur.
     *
     * Örnek:
     *
     * success
     * rejected
     * failed
     */
    @SerializedName("result")
    val result: String,

    /**
     * İşlemin başarılı bir doğrulama olarak tamamlanıp
     * tamamlanmadığını belirtir.
     */
    @SerializedName("is_successful")
    val isSuccessful: Boolean,

    /**
     * Challenge için yapılan toplam deneme sayısı.
     */
    @SerializedName("attempt_count")
    val attemptCount: Int,

    /**
     * İzin verilen maksimum deneme sayısı.
     */
    @SerializedName("max_attempts")
    val maxAttempts: Int,

    /**
     * Mobil cihaz imzasının başarıyla doğrulanıp
     * doğrulanmadığını belirtir.
     *
     * Mobil onay veya ret akışında başarılı imza
     * doğrulamasından sonra true olur.
     */
    @SerializedName("device_signature_verified")
    val deviceSignatureVerified: Boolean,

    /**
     * Challenge tamamlanma zamanı.
     */
    @SerializedName("completed_at")
    val completedAt: String? = null,

    /**
     * Başarısızlık veya ret nedeni.
     */
    @SerializedName("failure_reason")
    val failureReason: String? = null,

    /**
     * Python servisinin hesapladığı risk puanı.
     */
    @SerializedName("risk_score")
    val riskScore: Int,

    /**
     * Risk seviyesi.
     *
     * Örnek:
     *
     * low
     * medium
     * high
     * critical
     */
    @SerializedName("risk_level")
    val riskLevel: String,
) {
    /**
     * Challenge'ın onaylanmış olup olmadığını belirtir.
     */
    fun isApproved(): Boolean {
        return (
                isSuccessful &&
                        status.equals(
                            other = "approved",
                            ignoreCase = true,
                        ) &&
                        result.equals(
                            other = "success",
                            ignoreCase = true,
                        )
                )
    }


    /**
     * Challenge'ın kullanıcı tarafından reddedilmiş
     * olup olmadığını belirtir.
     */
    fun isRejected(): Boolean {
        return (
                status.equals(
                    other = "rejected",
                    ignoreCase = true,
                ) ||
                        result.equals(
                            other = "rejected",
                            ignoreCase = true,
                        )
                )
    }


    /**
     * Kullanıcıya gösterilebilecek sonuç mesajını
     * oluşturur.
     */
    fun resolveResultMessage(): String {
        if (isApproved()) {
            return "Giriş isteği başarıyla onaylandı."
        }

        if (isRejected()) {
            return (
                    failureReason
                        .normalizeNullable()
                        ?: "Giriş isteği reddedildi."
                    )
        }

        return failureReason
            .normalizeNullable()
            ?: "Challenge işlemi tamamlanamadı."
    }
}


/*
 * =========================================================
 * CHALLENGE KARAR API RESPONSE TİPİ
 * =========================================================
 */


/**
 * Mobil challenge karar endpointinin tam response
 * tipidir.
 */
typealias ChallengeDecisionResponse =
        ApiResponse<ChallengeVerificationData>


/*
 * =========================================================
 * REPOSITORY SONUÇ MODELİ
 * =========================================================
 */


/**
 * Mobil challenge karar işleminin Repository katmanında
 * kullanılacak sade sonuç modelidir.
 */
sealed interface ChallengeDecisionResult {

    /**
     * Challenge kararı Python servisi tarafından
     * işlendi.
     */
    data class Success(
        val verification:
        ChallengeVerificationData,

        val message: String?,
    ) : ChallengeDecisionResult

    /**
     * Challenge kararı gönderilemedi veya sunucu
     * tarafından reddedildi.
     */
    data class Failure(
        val message: String,

        val httpStatusCode: Int? = null,
    ) : ChallengeDecisionResult
}


/*
 * =========================================================
 * İMZALANACAK PAYLOAD MODELİ
 * =========================================================
 */


/**
 * Mobil cihazın challenge kararını imzalamak için
 * ihtiyaç duyduğu temel alanları temsil eder.
 *
 * Bu sınıf doğrudan API'ye gönderilmez.
 *
 * Python tarafındaki
 * build_challenge_signing_payload fonksiyonuyla aynı
 * metin biçiminin oluşturulmasında kullanılacaktır.
 */
data class ChallengeSigningInput(
    /**
     * Challenge public ID değeri.
     */
    val challengePublicId: String,

    /**
     * WebSocket mesajındaki nonce değeri.
     */
    val nonce: String,

    /**
     * Ana backend kullanıcı kimliği.
     */
    val externalUserId: String,

    /**
     * Uygulama kurulum kimliği.
     */
    val installationId: String,

    /**
     * approve veya reject kararı.
     */
    val decision: ChallengeDecision,

    /**
     * Challenge son geçerlilik zamanı.
     */
    val expiresAt: String,
) {
    /**
     * Zorunlu alanları temel olarak kontrol eder.
     */
    fun validate(): ChallengeSigningInputValidationResult {
        if (challengePublicId.trim().isBlank()) {
            return ChallengeSigningInputValidationResult.Invalid(
                message = "Challenge kimliği boş olamaz.",
            )
        }

        if (nonce.trim().isBlank()) {
            return ChallengeSigningInputValidationResult.Invalid(
                message = "Challenge nonce değeri boş olamaz.",
            )
        }

        if (externalUserId.trim().isBlank()) {
            return ChallengeSigningInputValidationResult.Invalid(
                message = "Kullanıcı kimliği boş olamaz.",
            )
        }

        if (installationId.trim().length < 16) {
            return ChallengeSigningInputValidationResult.Invalid(
                message = (
                        "Installation ID en az 16 karakter "
                                + "olmalıdır."
                        ),
            )
        }

        if (expiresAt.trim().isBlank()) {
            return ChallengeSigningInputValidationResult.Invalid(
                message = (
                        "Challenge son geçerlilik zamanı "
                                + "boş olamaz."
                        ),
            )
        }

        return ChallengeSigningInputValidationResult.Valid
    }
}


/**
 * İmzalama girdisinin yerel doğrulama sonucudur.
 */
sealed interface ChallengeSigningInputValidationResult {

    data object Valid :
        ChallengeSigningInputValidationResult

    data class Invalid(
        val message: String,
    ) : ChallengeSigningInputValidationResult
}


/*
 * =========================================================
 * STRING YARDIMCISI
 * =========================================================
 */


/**
 * Nullable String değerini temizler.
 *
 * Null veya boş string için null döndürür.
 */
private fun String?.normalizeNullable(): String? {
    return this
        ?.trim()
        ?.takeIf {
            it.isNotBlank()
        }
}