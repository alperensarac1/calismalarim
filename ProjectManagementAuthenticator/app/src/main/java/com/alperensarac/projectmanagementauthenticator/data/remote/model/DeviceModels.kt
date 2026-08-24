package com.alperensarac.projectmanagementauthenticator.data.remote.model

import com.google.gson.annotations.SerializedName


/*
 * =========================================================
 * CİHAZ PLATFORMU
 * =========================================================
 */


/**
 * Authenticator servisine gönderilebilecek cihaz
 * platformlarını temsil eder.
 *
 * Bu uygulama Android olduğu için cihaz kaydında
 * varsayılan olarak ANDROID kullanılacaktır.
 */
enum class DevicePlatform(
    val apiValue: String,
) {
    ANDROID(
        apiValue = "android",
    ),

    IOS(
        apiValue = "ios",
    ),

    WINDOWS(
        apiValue = "windows",
    ),

    MACOS(
        apiValue = "macos",
    ),

    LINUX(
        apiValue = "linux",
    ),
}


/*
 * =========================================================
 * CİHAZ KAYIT REQUEST MODELİ
 * =========================================================
 */


/**
 * Android cihazını Python Authenticator servisine
 * kaydetmek için gönderilecek request modelidir.
 *
 * Endpoint:
 *
 * POST /api/devices/register
 *
 * Kullanıcı ID, e-posta veya görünen ad doğrudan
 * gönderilmez.
 *
 * Python servisi backendAccessToken değerini mevcut
 * .NET backend'in /api/Auth/me endpointine göndererek
 * kullanıcıyı güvenilir şekilde doğrular.
 */
data class DeviceRegistrationRequest(
    /**
     * Kullanıcının .NET backend girişinden aldığı
     * access token.
     *
     * Python alanı:
     *
     * backend_access_token
     */
    @SerializedName("backend_access_token")
    val backendAccessToken: String,

    /**
     * Uygulamanın mevcut kurulumuna ait benzersiz
     * installation ID.
     *
     * En az 16 karakter olmalıdır.
     */
    @SerializedName("installation_id")
    val installationId: String,

    /**
     * Cihaz platformu.
     *
     * Android uygulamasında "android" gönderilir.
     */
    @SerializedName("platform")
    val platform: String = DevicePlatform.ANDROID.apiValue,

    /**
     * Kullanıcının cihazı ayırt edebilmesi için
     * gösterilecek cihaz adı.
     *
     * Örnek:
     *
     * Alperen'in Android Telefonu
     */
    @SerializedName("device_name")
    val deviceName: String? = null,

    /**
     * Cihaz modeli.
     *
     * Örnek:
     *
     * SM-S918B
     * Pixel 8
     */
    @SerializedName("device_model")
    val deviceModel: String? = null,

    /**
     * Cihaz üreticisi.
     *
     * Örnek:
     *
     * Samsung
     * Google
     */
    @SerializedName("manufacturer")
    val manufacturer: String? = null,

    /**
     * İşletim sistemi adı.
     *
     * Android uygulamasında "Android" gönderilebilir.
     */
    @SerializedName("os_name")
    val osName: String? = null,

    /**
     * Android sürümü.
     *
     * Örnek:
     *
     * 14
     */
    @SerializedName("os_version")
    val osVersion: String? = null,

    /**
     * Mobil uygulamanın sürüm adı.
     *
     * Örnek:
     *
     * 1.0
     */
    @SerializedName("app_version")
    val appVersion: String? = null,

    /**
     * Cihazın dil ve bölge bilgisi.
     *
     * Örnek:
     *
     * tr-TR
     */
    @SerializedName("locale")
    val locale: String? = null,

    /**
     * Cihazın zaman dilimi.
     *
     * Örnek:
     *
     * Europe/Istanbul
     */
    @SerializedName("timezone_name")
    val timezoneName: String? = null,

    /**
     * Android Keystore içerisinde oluşturulacak anahtar
     * çiftinin public key değeridir.
     *
     * Private key hiçbir zaman cihazdan dışarı çıkmaz.
     *
     * Public key PEM biçiminde gönderilecektir.
     */
    @SerializedName("public_key")
    val publicKey: String,

    /**
     * Cihaz anahtarının algoritmasıdır.
     *
     * Python servisindeki varsayılan değer:
     *
     * ECDSA_P256_SHA256
     */
    @SerializedName("public_key_algorithm")
    val publicKeyAlgorithm: String =
        "ECDSA_P256_SHA256",

    /**
     * Android Key Attestation verisi.
     *
     * İlk sürümde kullanılmayacağı için null
     * gönderilebilir.
     */
    @SerializedName("key_attestation")
    val keyAttestation: String? = null,

    /**
     * Firebase Cloud Messaging push tokenı.
     *
     * Henüz FCM eklenmediği için null gönderilebilir.
     */
    @SerializedName("push_token")
    val pushToken: String? = null,
) {
    /**
     * Request içindeki string alanları temizlenmiş
     * yeni bir nesne olarak döndürür.
     */
    fun normalized(): DeviceRegistrationRequest {
        return copy(
            backendAccessToken =
            backendAccessToken.trim(),

            installationId =
            installationId.trim(),

            platform =
            platform.trim(),

            deviceName =
            deviceName.normalizeNullable(),

            deviceModel =
            deviceModel.normalizeNullable(),

            manufacturer =
            manufacturer.normalizeNullable(),

            osName =
            osName.normalizeNullable(),

            osVersion =
            osVersion.normalizeNullable(),

            appVersion =
            appVersion.normalizeNullable(),

            locale =
            locale.normalizeNullable(),

            timezoneName =
            timezoneName.normalizeNullable(),

            publicKey =
            publicKey.trim(),

            publicKeyAlgorithm =
            publicKeyAlgorithm.trim(),

            keyAttestation =
            keyAttestation.normalizeNullable(),

            pushToken =
            pushToken.normalizeNullable(),
        )
    }


    /**
     * API isteği gönderilmeden önce zorunlu alanları
     * yerel olarak kontrol eder.
     *
     * Bu kontrol Python tarafındaki Pydantic
     * doğrulamasının yerine geçmez.
     */
    fun validate(): DeviceRegistrationValidationResult {
        val normalizedRequest =
            normalized()

        if (
            normalizedRequest.backendAccessToken.length
            < 20
        ) {
            return DeviceRegistrationValidationResult.Invalid(
                message = (
                        "Backend access tokenı geçerli değil."
                        ),
            )
        }

        if (
            normalizedRequest.installationId.length
            < 16
        ) {
            return DeviceRegistrationValidationResult.Invalid(
                message = (
                        "Installation ID en az 16 karakter olmalıdır."
            ),
            )
        }

        if (
            normalizedRequest.platform.isBlank()
        ) {
            return DeviceRegistrationValidationResult.Invalid(
                message = "Cihaz platformu boş olamaz.",
            )
        }

        if (
            normalizedRequest.publicKey.length
            < 100
        ) {
            return DeviceRegistrationValidationResult.Invalid(
                message = (
                        "Public key bilgisi geçerli değil."
                        ),
            )
        }

        if (
            normalizedRequest.publicKeyAlgorithm
                .isBlank()
        ) {
            return DeviceRegistrationValidationResult.Invalid(
                message = (
                        "Public key algoritması boş olamaz."
                        ),
            )
        }

        return DeviceRegistrationValidationResult.Valid
    }
}


/**
 * Cihaz kayıt requestinin yerel doğrulama sonucudur.
 */
sealed interface DeviceRegistrationValidationResult {
    data object Valid :
        DeviceRegistrationValidationResult

    data class Invalid(
        val message: String,
    ) : DeviceRegistrationValidationResult
}


/*
 * =========================================================
 * KAYITLI CİHAZ RESPONSE MODELİ
 * =========================================================
 */


/**
 * Python servisinin güvenli kayıtlı cihaz response
 * modelidir.
 *
 * Public key, private key, push token ve key attestation
 * gibi hassas alanlar bu response içerisinde dönmez.
 */
data class RegisteredDeviceData(
    /**
     * Python servisindeki cihazın public UUID değeri.
     */
    @SerializedName("public_id")
    val publicId: String,

    /**
     * Uygulama kurulum kimliği.
     */
    @SerializedName("installation_id")
    val installationId: String,

    /**
     * Cihaz platformu.
     *
     * Örnek:
     *
     * android
     */
    @SerializedName("platform")
    val platform: String,

    /**
     * Cihazın kullanıcıya gösterilen adı.
     */
    @SerializedName("device_name")
    val deviceName: String? = null,

    /**
     * Cihaz modeli.
     */
    @SerializedName("device_model")
    val deviceModel: String? = null,

    /**
     * Cihaz üreticisi.
     */
    @SerializedName("manufacturer")
    val manufacturer: String? = null,

    /**
     * İşletim sistemi adı.
     */
    @SerializedName("os_name")
    val osName: String? = null,

    /**
     * İşletim sistemi sürümü.
     */
    @SerializedName("os_version")
    val osVersion: String? = null,

    /**
     * Authenticator uygulama sürümü.
     */
    @SerializedName("app_version")
    val appVersion: String? = null,

    /**
     * Cihaz dil ve bölge bilgisi.
     */
    @SerializedName("locale")
    val locale: String? = null,

    /**
     * Cihaz zaman dilimi.
     */
    @SerializedName("timezone_name")
    val timezoneName: String? = null,

    /**
     * Kayıtlı anahtar algoritması.
     */
    @SerializedName("key_algorithm")
    val keyAlgorithm: String? = null,

    /**
     * Public key parmak izi.
     */
    @SerializedName("public_key_fingerprint")
    val publicKeyFingerprint: String? = null,

    /**
     * Cihaz anahtarının oluşturulma tarihi.
     */
    @SerializedName("key_created_at")
    val keyCreatedAt: String? = null,

    /**
     * Key attestation bilgisinin sunucu tarafından
     * doğrulanıp doğrulanmadığını belirtir.
     */
    @SerializedName("key_attestation_verified")
    val keyAttestationVerified: Boolean = false,

    /**
     * Cihazın aktif olup olmadığını belirtir.
     */
    @SerializedName("is_active")
    val isActive: Boolean,

    /**
     * Cihaz kayıt edilirken görülen IP adresi.
     */
    @SerializedName("registered_ip")
    val registeredIp: String? = null,

    /**
     * Cihazdan alınan son IP adresi.
     */
    @SerializedName("last_ip")
    val lastIp: String? = null,

    /**
     * Cihaz kayıt tarihi.
     */
    @SerializedName("registered_at")
    val registeredAt: String,

    /**
     * Cihazın son görülme zamanı.
     */
    @SerializedName("last_seen_at")
    val lastSeenAt: String? = null,

    /**
     * Cihazın devre dışı bırakılma zamanı.
     */
    @SerializedName("revoked_at")
    val revokedAt: String? = null,
) {
    /**
     * Ekranda gösterilecek cihaz başlığını üretir.
     */
    fun resolveDisplayName(): String {
        return deviceName.normalizeNullable()
            ?: deviceModel.normalizeNullable()
            ?: manufacturer.normalizeNullable()
            ?: "Authenticator cihazı"
    }


    /**
     * Bu cihazın kullanılabilir durumda olup
     * olmadığını belirtir.
     */
    fun isUsable(): Boolean {
        return isActive && revokedAt == null
    }
}


/*
 * =========================================================
 * CİHAZ KAYIT RESPONSE MODELİ
 * =========================================================
 */


/**
 * POST /api/devices/register endpointinin data alanında
 * dönen cevaptır.
 */
data class DeviceRegistrationData(
    /**
     * Kaydedilen veya güncellenen cihaz bilgisi.
     */
    @SerializedName("device")
    val device: RegisteredDeviceData,

    /**
     * Python servisinin cihaz için oluşturduğu JWT.
     *
     * Bundan sonraki cihaz endpointleri ve WebSocket
     * bağlantısı bu tokenla doğrulanacaktır.
     */
    @SerializedName("device_access_token")
    val deviceAccessToken: String,

    /**
     * Token türü.
     *
     * Python servisi "bearer" döndürür.
     */
    @SerializedName("token_type")
    val tokenType: String = "bearer",

    /**
     * Cihaz tokenının son kullanma zamanı.
     */
    @SerializedName("expires_at")
    val expiresAt: String,
) {
    /**
     * Authorization başlığı oluşturur.
     */
    fun createAuthorizationHeader(): String {
        val normalizedToken =
            deviceAccessToken.trim()

        require(
            normalizedToken.isNotBlank(),
        ) {
            "Cihaz access tokenı boş olamaz."
        }

        return "Bearer $normalizedToken"
    }
}


/*
 * =========================================================
 * HEARTBEAT REQUEST MODELİ
 * =========================================================
 */


/**
 * Mobil cihazın hâlâ aktif olduğunu Python servisine
 * bildiren request modelidir.
 *
 * Endpoint:
 *
 * POST /api/devices/heartbeat
 */
data class DeviceHeartbeatRequest(
    /**
     * Cihaz kaydı sırasında kullanılan installation ID.
     *
     * Token içindeki ve veritabanındaki değerle aynı
     * olmalıdır.
     */
    @SerializedName("installation_id")
    val installationId: String,

    /**
     * Authenticator uygulamasının güncel sürümü.
     */
    @SerializedName("app_version")
    val appVersion: String? = null,

    /**
     * Android işletim sistemi sürümü.
     */
    @SerializedName("os_version")
    val osVersion: String? = null,

    /**
     * Firebase Cloud Messaging tokenı.
     *
     * FCM eklenene kadar null olabilir.
     */
    @SerializedName("push_token")
    val pushToken: String? = null,
) {
    /**
     * Heartbeat request alanlarını temizler.
     */
    fun normalized(): DeviceHeartbeatRequest {
        return copy(
            installationId =
            installationId.trim(),

            appVersion =
            appVersion.normalizeNullable(),

            osVersion =
            osVersion.normalizeNullable(),

            pushToken =
            pushToken.normalizeNullable(),
        )
    }


    /**
     * Installation ID alanını yerel olarak doğrular.
     */
    fun validate(): DeviceHeartbeatValidationResult {
        if (
            installationId.trim().length < 16
        ) {
            return DeviceHeartbeatValidationResult.Invalid(
                message = (
                        "Installation ID en az 16 karakter olmalıdır."
            ),
            )
        }

        return DeviceHeartbeatValidationResult.Valid
    }
}


/**
 * Heartbeat requestinin yerel doğrulama sonucudur.
 */
sealed interface DeviceHeartbeatValidationResult {
    data object Valid :
        DeviceHeartbeatValidationResult

    data class Invalid(
        val message: String,
    ) : DeviceHeartbeatValidationResult
}


/*
 * =========================================================
 * HEARTBEAT RESPONSE MODELİ
 * =========================================================
 */


/**
 * POST /api/devices/heartbeat endpointinin data alanında
 * dönen cevaptır.
 */
data class DeviceHeartbeatData(
    /**
     * Güncellenen cihaz bilgisi.
     */
    @SerializedName("device")
    val device: RegisteredDeviceData,

    /**
     * Heartbeat güncelleme zamanı.
     */
    @SerializedName("updated_at")
    val updatedAt: String,
)


/*
 * =========================================================
 * CİHAZ LİSTE RESPONSE MODELİ
 * =========================================================
 */


/**
 * GET /api/devices/my-devices endpointinin data alanında
 * dönen cevaptır.
 */
data class DeviceListData(
    /**
     * Kullanıcıya ait cihaz listesi.
     */
    @SerializedName("items")
    val items: List<RegisteredDeviceData> = emptyList(),

    /**
     * Toplam cihaz sayısı.
     */
    @SerializedName("total_count")
    val totalCount: Int = 0,
) {
    /**
     * Aktif cihazları döndürür.
     */
    fun getActiveDevices():
            List<RegisteredDeviceData> {
        return items.filter {
                device ->

            device.isUsable()
        }
    }


    /**
     * Kullanıcının en az bir aktif cihazı olup
     * olmadığını belirtir.
     */
    fun hasActiveDevice(): Boolean {
        return items.any {
                device ->

            device.isUsable()
        }
    }
}


/*
 * =========================================================
 * REPOSITORY SONUÇ MODELLERİ
 * =========================================================
 */


/**
 * Cihaz kayıt işleminin Repository katmanında
 * kullanılacak sade sonuç modelidir.
 */
sealed interface DeviceRegistrationResult {
    /**
     * Cihaz başarıyla kaydedildi veya güncellendi.
     */
    data class Success(
        val device: RegisteredDeviceData,
        val deviceAccessToken: String,
        val expiresAt: String,
        val message: String?,
    ) : DeviceRegistrationResult

    /**
     * Cihaz kayıt işlemi başarısız oldu.
     */
    data class Failure(
        val message: String,
        val httpStatusCode: Int? = null,
    ) : DeviceRegistrationResult
}


/**
 * Heartbeat işleminin Repository katmanında
 * kullanılacak sade sonuç modelidir.
 */
sealed interface DeviceHeartbeatResult {
    /**
     * Heartbeat başarıyla gönderildi.
     */
    data class Success(
        val device: RegisteredDeviceData,
        val updatedAt: String,
    ) : DeviceHeartbeatResult

    /**
     * Heartbeat işlemi başarısız oldu.
     */
    data class Failure(
        val message: String,
        val httpStatusCode: Int? = null,
    ) : DeviceHeartbeatResult
}


/*
 * =========================================================
 * STRING YARDIMCISI
 * =========================================================
 */


/**
 * Nullable String değerini temizler.
 *
 * Null veya boş değer için null döndürür.
 */
private fun String?.normalizeNullable(): String? {
    return this
        ?.trim()
        ?.takeIf {
            it.isNotBlank()
        }
}