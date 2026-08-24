package com.alperensarac.projectmanagementauthenticator.data.remote.model

import com.google.gson.annotations.SerializedName


/*
 * =========================================================
 * WEBSOCKET MESAJ TİPLERİ
 * =========================================================
 */


/**
 * Python Authenticator servisi ile Android uygulaması
 * arasında kullanılan WebSocket mesaj tiplerini merkezi
 * olarak tutar.
 *
 * Gelen JSON mesajı işlenirken önce "type" alanı okunur,
 * ardından ilgili modele dönüştürülür.
 */
object WebSocketMessageType {

    /**
     * Android uygulamasının bağlantı açıldıktan sonra
     * sunucuya gönderdiği cihaz doğrulama mesajı.
     */
    const val AUTHENTICATE =
        "authenticate"


    /**
     * Bazı WebSocket sunucularının bağlantı açıldıktan
     * sonra gönderebileceği genel bağlantı mesajı.
     */
    const val CONNECTED =
        "connected"


    /**
     * Cihaz tokenı ve installation ID başarıyla
     * doğrulandığında sunucunun gönderdiği mesaj.
     */
    const val AUTHENTICATED =
        "authenticated"


    /**
     * Mobil uygulamaya yeni bir giriş doğrulama isteği
     * gönderildiğini belirtir.
     */
    const val AUTHENTICATION_CHALLENGE =
        "authentication_challenge"


    /**
     * Aktif challenge'ın iptal edildiğini belirtir.
     */
    const val CHALLENGE_CANCELLED =
        "challenge_cancelled"


    /**
     * Challenge onay veya ret işleminin sonucunu
     * bildirir.
     *
     * Python challenge verification routerı karar
     * tamamlandıktan sonra bu mesajı gönderebilir.
     */
    const val CHALLENGE_RESULT =
        "challenge_result"


    /**
     * Android uygulamasının bağlantıyı canlı tutmak için
     * gönderdiği uygulama seviyesindeki heartbeat.
     */
    const val HEARTBEAT =
        "heartbeat"


    /**
     * Python sunucusunun heartbeat mesajına verdiği
     * cevap.
     */
    const val HEARTBEAT_ACK =
        "heartbeat_ack"


    /**
     * Kullanıcının WebSocket bağlantısını bilinçli
     * biçimde kapatmak istediğini belirtir.
     */
    const val DISCONNECT =
        "disconnect"


    /**
     * Sunucunun disconnect mesajına verdiği cevap.
     */
    const val DISCONNECT_ACK =
        "disconnect_ack"


    /**
     * WebSocket seviyesinde hata oluştuğunu belirtir.
     */
    const val ERROR =
        "error"
}


/*
 * =========================================================
 * ORTAK MESAJ ZARFI
 * =========================================================
 */


/**
 * Gelen WebSocket JSON mesajının yalnızca "type"
 * alanını okumak için kullanılan sade modeldir.
 *
 * Örnek:
 *
 * {
 *   "type": "authentication_challenge",
 *   ...
 * }
 */
data class WebSocketMessageEnvelope(
    @SerializedName("type")
    val type: String? = null,
)


/*
 * =========================================================
 * CİHAZ KİMLİK DOĞRULAMA MESAJI
 * =========================================================
 */


/**
 * WebSocket bağlantısı açıldıktan sonra Android
 * uygulamasının göndermesi gereken ilk mesajdır.
 *
 * Python sunucusu bu mesajı en geç 15 saniye içerisinde
 * beklemektedir.
 *
 * JSON:
 *
 * {
 *   "type": "authenticate",
 *   "installation_id": "...",
 *   "device_access_token": "..."
 * }
 */
data class WebSocketAuthenticateMessage(
    @SerializedName("type")
    val type: String =
        WebSocketMessageType.AUTHENTICATE,

    /**
     * DataStore içerisinde saklanan uygulama kurulum
     * kimliği.
     */
    @SerializedName("installation_id")
    val installationId: String,

    /**
     * Python cihaz kayıt endpointinden alınan device
     * access token.
     *
     * Bearer ön eki olmadan gönderilir.
     */
    @SerializedName("device_access_token")
    val deviceAccessToken: String,
) {

    /**
     * String alanlarını temizlenmiş biçimde döndürür.
     */
    fun normalized(): WebSocketAuthenticateMessage {
        return copy(
            installationId =
            installationId.trim(),

            deviceAccessToken =
            deviceAccessToken.trim(),
        )
    }


    /**
     * Mesaj gönderilmeden önce temel yerel doğrulama
     * yapar.
     */
    fun validate(): WebSocketMessageValidationResult {
        val normalizedMessage =
            normalized()


        if (
            normalizedMessage.installationId.length <
            16
        ) {
            return WebSocketMessageValidationResult.Invalid(
                message = (
                        "Installation ID en az 16 karakter "
                                + "olmalıdır."
                        ),
            )
        }


        if (
            normalizedMessage.deviceAccessToken.length <
            20
        ) {
            return WebSocketMessageValidationResult.Invalid(
                message =
                "Device access token geçerli değil.",
            )
        }


        return WebSocketMessageValidationResult.Valid
    }
}


/*
 * =========================================================
 * HEARTBEAT MESAJI
 * =========================================================
 */


/**
 * WebSocket bağlantısını uygulama seviyesinde canlı
 * tutmak için gönderilen mesajdır.
 *
 * JSON:
 *
 * {
 *   "type": "heartbeat",
 *   "sent_at": "2026-08-03T10:40:00.000Z"
 * }
 */
data class WebSocketHeartbeatMessage(
    @SerializedName("type")
    val type: String =
        WebSocketMessageType.HEARTBEAT,

    /**
     * Heartbeat mesajının UTC oluşturulma zamanı.
     */
    @SerializedName("sent_at")
    val sentAt: String,
)


/*
 * =========================================================
 * DISCONNECT MESAJI
 * =========================================================
 */


/**
 * Android uygulamasının WebSocket bağlantısını bilinçli
 * şekilde kapatmadan önce gönderebileceği mesajdır.
 */
data class WebSocketDisconnectMessage(
    @SerializedName("type")
    val type: String =
        WebSocketMessageType.DISCONNECT,
)


/*
 * =========================================================
 * AUTHENTICATION CHALLENGE MESAJI
 * =========================================================
 */


/**
 * Python sunucusunun Android Authenticator uygulamasına
 * gönderdiği yeni giriş doğrulama isteğidir.
 *
 * Örnek JSON:
 *
 * {
 *   "type": "authentication_challenge",
 *   "challenge_public_id": "...",
 *   "method": "mobile_approval",
 *   "nonce": "...",
 *   "external_user_id": "...",
 *   "display_name": "...",
 *   "email": "...",
 *   "device_public_id": "...",
 *   "request_ip": "...",
 *   "request_origin": "react-web",
 *   "created_at": "...",
 *   "expires_at": "...",
 *   "one_time_code": "987456"
 * }
 */
data class WebSocketChallengeMessage(
    @SerializedName("type")
    val type: String =
        WebSocketMessageType.AUTHENTICATION_CHALLENGE,

    /**
     * Challenge'ın dışarıya açılan benzersiz kimliği.
     */
    @SerializedName("challenge_public_id")
    val challengePublicId: String,

    /**
     * Challenge doğrulama yöntemi.
     *
     * Örnek:
     *
     * mobile_approval
     * one_time_code
     */
    @SerializedName("method")
    val method: String,

    /**
     * Cihaz imzasında kullanılacak tek kullanımlık
     * rastgele değer.
     */
    @SerializedName("nonce")
    val nonce: String,

    /**
     * Ana .NET backend üzerindeki kullanıcı kimliği.
     */
    @SerializedName("external_user_id")
    val externalUserId: String,

    /**
     * Kullanıcının ekranda gösterilecek adı.
     */
    @SerializedName("display_name")
    val displayName: String? = null,

    /**
     * Kullanıcının e-posta adresi.
     */
    @SerializedName("email")
    val email: String? = null,

    /**
     * Challenge'ın hedeflendiği cihaz kimliği.
     */
    @SerializedName("device_public_id")
    val devicePublicId: String,

    /**
     * Giriş isteğinin geldiği IP adresi.
     */
    @SerializedName("request_ip")
    val requestIp: String? = null,

    /**
     * İsteğin geldiği istemci veya uygulama.
     *
     * Örnek:
     *
     * react-web
     */
    @SerializedName("request_origin")
    val requestOrigin: String? = null,

    /**
     * Challenge oluşturulma zamanı.
     */
    @SerializedName("created_at")
    val createdAt: String,

    /**
     * Challenge geçerlilik bitiş zamanı.
     *
     * Cihaz imzası oluşturulurken bu değer Python ile
     * birebir aynı UTC metnine dönüştürülür.
     */
    @SerializedName("expires_at")
    val expiresAt: String,

    /**
     * Demo veya kod doğrulama yöntemi için gösterilecek
     * tek kullanımlık kod.
     */
    @SerializedName("one_time_code")
    val oneTimeCode: String? = null,
) {

    /**
     * Challenge üzerinde gösterilecek kullanıcı adını
     * belirler.
     */
    fun resolveUserDisplayName(): String {
        val normalizedDisplayName =
            displayName
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }


        if (normalizedDisplayName != null) {
            return normalizedDisplayName
        }


        val normalizedEmail =
            email
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }


        if (normalizedEmail != null) {
            return normalizedEmail
        }


        return "Project Management kullanıcısı"
    }


    /**
     * Ekranda gösterilecek request kaynağını döndürür.
     */
    fun resolveRequestOrigin(): String {
        return requestOrigin
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
            ?: "Bilinmeyen uygulama"
    }


    /**
     * Challenge'ın tek kullanımlık kod içerip
     * içermediğini belirtir.
     */
    fun hasOneTimeCode(): Boolean {
        return !oneTimeCode
            .isNullOrBlank()
    }


    /**
     * Challenge mesajının temel zorunlu alanlarını
     * doğrular.
     */
    fun validate(): WebSocketMessageValidationResult {
        if (
            challengePublicId.trim().isBlank()
        ) {
            return WebSocketMessageValidationResult.Invalid(
                message =
                "Challenge kimliği boş olamaz.",
            )
        }


        if (
            method.trim().isBlank()
        ) {
            return WebSocketMessageValidationResult.Invalid(
                message = (
                        "Challenge doğrulama yöntemi "
                                + "boş olamaz."
                        ),
            )
        }


        if (
            nonce.trim().isBlank()
        ) {
            return WebSocketMessageValidationResult.Invalid(
                message =
                "Challenge nonce değeri boş olamaz.",
            )
        }


        if (
            externalUserId.trim().isBlank()
        ) {
            return WebSocketMessageValidationResult.Invalid(
                message =
                "Kullanıcı kimliği boş olamaz.",
            )
        }


        if (
            devicePublicId.trim().isBlank()
        ) {
            return WebSocketMessageValidationResult.Invalid(
                message =
                "Cihaz kimliği boş olamaz.",
            )
        }


        if (
            createdAt.trim().isBlank()
        ) {
            return WebSocketMessageValidationResult.Invalid(
                message = (
                        "Challenge oluşturulma zamanı "
                                + "bulunamadı."
                        ),
            )
        }


        if (
            expiresAt.trim().isBlank()
        ) {
            return WebSocketMessageValidationResult.Invalid(
                message = (
                        "Challenge son geçerlilik zamanı "
                                + "bulunamadı."
                        ),
            )
        }


        return WebSocketMessageValidationResult.Valid
    }
}


/*
 * =========================================================
 * CHALLENGE İPTAL MESAJI
 * =========================================================
 */


/**
 * Challenge web veya backend tarafında iptal edildiğinde
 * Android uygulamasına gönderilir.
 */
data class WebSocketChallengeCancelledMessage(
    @SerializedName("type")
    val type: String =
        WebSocketMessageType.CHALLENGE_CANCELLED,

    /**
     * İptal edilen challenge kimliği.
     */
    @SerializedName("challenge_public_id")
    val challengePublicId: String,

    /**
     * Opsiyonel iptal açıklaması.
     */
    @SerializedName("reason")
    val reason: String? = null,
) {

    /**
     * Kullanıcıya gösterilecek iptal açıklamasını
     * döndürür.
     */
    fun resolveReason(): String {
        return reason
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
            ?: "Doğrulama isteği iptal edildi."
    }
}


/*
 * =========================================================
 * CHALLENGE SONUÇ MESAJI
 * =========================================================
 */


/**
 * Challenge doğrulama işlemi tamamlandığında Python
 * sunucusunun WebSocket üzerinden gönderdiği sonuç
 * mesajıdır.
 *
 * Bu mesaj karar HTTP isteğinin sonucundan bağımsız
 * olarak açık WebSocket bağlantısına da gelebilir.
 */
data class WebSocketChallengeResultMessage(
    @SerializedName("type")
    val type: String =
        WebSocketMessageType.CHALLENGE_RESULT,

    @SerializedName("challenge_public_id")
    val challengePublicId: String,

    /**
     * Challenge'ın son durumu.
     *
     * Örnek:
     *
     * approved
     * rejected
     * expired
     */
    @SerializedName("status")
    val status: String,

    /**
     * Doğrulama denemesinin sonucu.
     *
     * Örnek:
     *
     * success
     * rejected
     * failed
     */
    @SerializedName("result")
    val result: String,

    @SerializedName("is_successful")
    val isSuccessful: Boolean,

    @SerializedName("attempt_count")
    val attemptCount: Int,

    @SerializedName("max_attempts")
    val maxAttempts: Int,

    @SerializedName("completed_at")
    val completedAt: String? = null,
) {

    /**
     * Challenge başarılı biçimde onaylandıysa true
     * döndürür.
     */
    fun isApproved(): Boolean {
        return (
                isSuccessful &&
                        status.equals(
                            other = "approved",
                            ignoreCase = true,
                        )
                )
    }


    /**
     * Challenge reddedildiyse true döndürür.
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
     * Kullanıcıya gösterilebilecek sade sonuç mesajını
     * oluşturur.
     */
    fun resolveMessage(): String {
        return when {
            isApproved() -> {
                "Giriş isteği başarıyla onaylandı."
            }

            isRejected() -> {
                "Giriş isteği reddedildi."
            }

            status.equals(
                other = "expired",
                ignoreCase = true,
            ) -> {
                "Doğrulama isteğinin süresi doldu."
            }

            else -> {
                "Doğrulama isteği tamamlandı: $status"
            }
        }
    }
}


/*
 * =========================================================
 * GENEL SUNUCU MESAJI
 * =========================================================
 */


/**
 * Python sunucusundan gelen genel bilgi, heartbeat ve
 * hata mesajlarını temsil eder.
 *
 * Python mesajlarında bazı alanlar farklı adlarla
 * gelebilir:
 *
 * authenticated:
 * connected_at
 *
 * heartbeat_ack:
 * sent_at
 *
 * Bu farklılık AuthenticatorWebSocketManager içerisinde
 * normalize edilir.
 */
data class WebSocketServerMessage(
    @SerializedName("type")
    val type: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("sent_at")
    val sentAt: String,

    /**
     * Python hata mesajında gelebilecek teknik hata
     * kodudur.
     *
     * Örnek:
     *
     * invalid_device_token
     * device_token_expired
     */
    @SerializedName("code")
    val code: String? = null,
) {

    /**
     * Mesaj hata türündeyse true döndürür.
     */
    fun isError(): Boolean {
        return type.equals(
            other = WebSocketMessageType.ERROR,
            ignoreCase = true,
        )
    }


    /**
     * Cihaz WebSocket üzerinde doğrulandıysa true
     * döndürür.
     */
    fun isAuthenticated(): Boolean {
        return type.equals(
            other = WebSocketMessageType.AUTHENTICATED,
            ignoreCase = true,
        )
    }


    /**
     * Heartbeat cevabıysa true döndürür.
     */
    fun isHeartbeatAcknowledgement(): Boolean {
        return type.equals(
            other = WebSocketMessageType.HEARTBEAT_ACK,
            ignoreCase = true,
        )
    }
}


/*
 * =========================================================
 * MESAJ DOĞRULAMA SONUCU
 * =========================================================
 */


/**
 * WebSocket mesajlarının yerel doğrulama sonucunu
 * temsil eder.
 */
sealed interface WebSocketMessageValidationResult {

    /**
     * Mesaj temel kontrollerden geçti.
     */
    data object Valid :
        WebSocketMessageValidationResult


    /**
     * Mesaj alanlarından biri geçersiz.
     */
    data class Invalid(
        val message: String,
    ) : WebSocketMessageValidationResult
}


/*
 * =========================================================
 * WEBSOCKET BAĞLANTI DURUMU
 * =========================================================
 */


/**
 * Android uygulamasındaki WebSocket bağlantısının güncel
 * durumunu temsil eder.
 */
sealed interface AuthenticatorWebSocketState {

    /**
     * Henüz bağlantı başlatılmadı.
     */
    data object Idle :
        AuthenticatorWebSocketState


    /**
     * WebSocket bağlantısı kurulmaya çalışılıyor.
     */
    data object Connecting :
        AuthenticatorWebSocketState


    /**
     * Socket açıldı ancak cihaz henüz doğrulanmadı.
     */
    data object Connected :
        AuthenticatorWebSocketState


    /**
     * Cihaz tokenı ve installation ID doğrulandı.
     */
    data object Authenticated :
        AuthenticatorWebSocketState


    /**
     * WebSocket bağlantısı kapatıldı.
     */
    data class Disconnected(
        val reason: String? = null,
    ) : AuthenticatorWebSocketState


    /**
     * Bağlantı veya mesaj işleme sırasında hata oluştu.
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : AuthenticatorWebSocketState
}


/*
 * =========================================================
 * WEBSOCKET EVENTLERİ
 * =========================================================
 */


/**
 * AuthenticatorWebSocketManager tarafından ViewModel'e
 * yayınlanacak tek seferlik olayları temsil eder.
 *
 * Bağlantı durumu StateFlow ile, mesaj olayları ise
 * SharedFlow ile yayınlanır.
 */
sealed interface AuthenticatorWebSocketEvent {

    /**
     * Yeni challenge mesajı alındı.
     */
    data class ChallengeReceived(
        val challenge:
        WebSocketChallengeMessage,
    ) : AuthenticatorWebSocketEvent


    /**
     * Aktif challenge iptal edildi.
     */
    data class ChallengeCancelled(
        val message:
        WebSocketChallengeCancelledMessage,
    ) : AuthenticatorWebSocketEvent


    /**
     * Challenge sonucu WebSocket üzerinden bildirildi.
     */
    data class ChallengeResultReceived(
        val result:
        WebSocketChallengeResultMessage,
    ) : AuthenticatorWebSocketEvent


    /**
     * Genel sunucu mesajı alındı.
     */
    data class ServerMessageReceived(
        val message:
        WebSocketServerMessage,
    ) : AuthenticatorWebSocketEvent


    /**
     * Gelen WebSocket JSON mesajı çözümlenemedi.
     */
    data class MessageParsingFailed(
        val rawMessage: String,
        val errorMessage: String,
    ) : AuthenticatorWebSocketEvent
}