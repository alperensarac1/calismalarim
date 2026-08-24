package com.alperensarac.projectmanagementauthenticator.data.websocket

import com.alperensarac.projectmanagementauthenticator.data.local.AuthSessionManager
import com.alperensarac.projectmanagementauthenticator.data.remote.NetworkModule
import com.alperensarac.projectmanagementauthenticator.data.remote.model.AuthenticatorWebSocketEvent
import com.alperensarac.projectmanagementauthenticator.data.remote.model.AuthenticatorWebSocketState
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketAuthenticateMessage
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketChallengeCancelledMessage
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketChallengeMessage
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketChallengeResultMessage
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketDisconnectMessage
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketHeartbeatMessage
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketMessageEnvelope
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketMessageType
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketMessageValidationResult
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketServerMessage

import com.google.gson.JsonObject
import com.google.gson.JsonParser

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

import okio.ByteString

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicBoolean


/*
 * =========================================================
 * AUTHENTICATOR WEBSOCKET MANAGER
 * =========================================================
 */


/**
 * Python Authenticator servisiyle kurulan WebSocket
 * bağlantısını yöneten sınıftır.
 *
 * Temel sorumlulukları:
 *
 * - Python WebSocket endpointine bağlanmak
 * - Bağlantı açıldığında cihaz kimliğini doğrulamak
 * - Belirli aralıklarla heartbeat göndermek
 * - Yeni authentication challenge mesajlarını dinlemek
 * - Challenge iptal mesajlarını işlemek
 * - Challenge sonuç mesajlarını işlemek
 * - Bağlantı durumunu StateFlow ile yayınlamak
 * - Tek seferlik olayları SharedFlow ile yayınlamak
 * - Beklenmeyen kopmalarda yeniden bağlanmak
 *
 * WebSocket endpointi:
 *
 * ws://10.203.83.58:8090/ws/device
 */
class AuthenticatorWebSocketManager(
    private val authSessionManager: AuthSessionManager,

    private val webSocketClient: OkHttpClient =
        NetworkModule.webSocketClient,
) {

    /*
     * =====================================================
     * COROUTINE SCOPE
     * =====================================================
     */


    /**
     * WebSocket heartbeat ve yeniden bağlantı işlemleri
     * için kullanılan bağımsız CoroutineScope.
     */
    private val managerScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO,
        )


    /*
     * =====================================================
     * STATE VE EVENT AKIŞLARI
     * =====================================================
     */


    private val _connectionState =
        MutableStateFlow<AuthenticatorWebSocketState>(
            AuthenticatorWebSocketState.Idle,
        )


    /**
     * WebSocket bağlantısının güncel durumudur.
     */
    val connectionState:
            StateFlow<AuthenticatorWebSocketState> =
        _connectionState.asStateFlow()


    private val _events =
        MutableSharedFlow<AuthenticatorWebSocketEvent>(
            extraBufferCapacity = 20,
        )


    /**
     * Challenge ve sunucu mesajları gibi tek seferlik
     * olayları yayınlar.
     */
    val events:
            SharedFlow<AuthenticatorWebSocketEvent> =
        _events.asSharedFlow()


    /*
     * =====================================================
     * BAĞLANTI DEĞİŞKENLERİ
     * =====================================================
     */


    /**
     * Aktif OkHttp WebSocket nesnesi.
     */
    @Volatile
    private var activeWebSocket: WebSocket? =
        null


    /**
     * Heartbeat gönderen coroutine işi.
     */
    private var heartbeatJob: Job? =
        null


    /**
     * Otomatik yeniden bağlantı coroutine işi.
     */
    private var reconnectJob: Job? =
        null


    /**
     * Kullanıcının bağlantıyı bilinçli şekilde kapatıp
     * kapatmadığını belirtir.
     */
    private val manuallyDisconnected =
        AtomicBoolean(
            false,
        )


    /**
     * Aynı anda birden fazla bağlantı başlatılmasını
     * önler.
     */
    private val connectionInProgress =
        AtomicBoolean(
            false,
        )


    /**
     * Art arda başarısız bağlantılarda kullanılacak
     * yeniden bağlantı deneme sayısı.
     */
    private var reconnectAttempt =
        0


    /*
     * =====================================================
     * BAĞLANTI BAŞLATMA
     * =====================================================
     */


    /**
     * DataStore içerisindeki installation ID ve device
     * access token bilgilerini kullanarak WebSocket
     * bağlantısını başlatır.
     */
    fun connect() {
        managerScope.launch {
            connectInternal()
        }
    }


    /**
     * WebSocket bağlantısını coroutine içerisinde
     * oluşturur.
     */
    private suspend fun connectInternal() {
        if (
            !connectionInProgress.compareAndSet(
                false,
                true,
            )
        ) {
            return
        }


        /*
         * Bağlantı zaten kurulmuşsa ikinci bağlantı
         * başlatılmaz.
         */
        when (_connectionState.value) {
            AuthenticatorWebSocketState.Connecting,
            AuthenticatorWebSocketState.Connected,
            AuthenticatorWebSocketState.Authenticated,
                -> {
                connectionInProgress.set(
                    false,
                )

                return
            }

            else -> {
                // Yeni bağlantı başlatılabilir.
            }
        }


        manuallyDisconnected.set(
            false,
        )


        _connectionState.value =
            AuthenticatorWebSocketState.Connecting


        val session =
            try {
                authSessionManager
                    .getCurrentSession()
            } catch (exception: Exception) {
                connectionInProgress.set(
                    false,
                )

                publishConnectionError(
                    message =
                    "Cihaz oturum bilgileri okunamadı.",

                    throwable =
                    exception,
                )

                return
            }


        val installationId =
            session.installationId
                .trim()
                .takeIf {
                    it.isNotBlank()
                }


        val deviceAccessToken =
            session.deviceAccessToken
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }


        if (installationId == null) {
            connectionInProgress.set(
                false,
            )

            publishConnectionError(
                message = (
                        "WebSocket bağlantısı için "
                                + "installation ID bulunamadı."
                        ),
            )

            return
        }


        if (deviceAccessToken == null) {
            connectionInProgress.set(
                false,
            )

            publishConnectionError(
                message = (
                        "WebSocket bağlantısı için cihaz "
                                + "access tokenı bulunamadı."
                        ),
            )

            return
        }


        val authenticateMessage =
            WebSocketAuthenticateMessage(
                installationId =
                installationId,

                deviceAccessToken =
                deviceAccessToken,
            ).normalized()


        when (
            val validationResult =
                authenticateMessage.validate()
        ) {
            WebSocketMessageValidationResult.Valid -> {
                // Bağlantı kurulabilir.
            }

            is WebSocketMessageValidationResult.Invalid -> {
                connectionInProgress.set(
                    false,
                )

                publishConnectionError(
                    message =
                    validationResult.message,
                )

                return
            }
        }


        /*
         * Önceki socket tam kapanmamışsa iptal edilir.
         */
        activeWebSocket?.cancel()

        activeWebSocket =
            null


        val request =
            Request.Builder()
                .url(
                    buildWebSocketUrl(),
                )
                .build()


        val listener =
            createWebSocketListener(
                authenticateMessage =
                authenticateMessage,
            )


        activeWebSocket =
            webSocketClient.newWebSocket(
                request,
                listener,
            )
    }


    /*
     * =====================================================
     * WEBSOCKET LISTENER
     * =====================================================
     */


    /**
     * OkHttp WebSocket olaylarını yöneten listener
     * nesnesini oluşturur.
     */
    private fun createWebSocketListener(
        authenticateMessage:
        WebSocketAuthenticateMessage,
    ): WebSocketListener {
        return object : WebSocketListener() {

            /**
             * Socket bağlantısı başarıyla açıldığında
             * çağrılır.
             */
            override fun onOpen(
                webSocket: WebSocket,
                response: Response,
            ) {
                connectionInProgress.set(
                    false,
                )


                activeWebSocket =
                    webSocket


                _connectionState.value =
                    AuthenticatorWebSocketState.Connected


                /*
                 * Python servisi ilk mesaj olarak cihaz
                 * doğrulama mesajını bekler.
                 */
                val authenticationJson =
                    NetworkModule.gson.toJson(
                        authenticateMessage,
                    )


                val sent =
                    webSocket.send(
                        authenticationJson,
                    )


                if (!sent) {
                    publishConnectionError(
                        message = (
                                "WebSocket kimlik doğrulama "
                                        + "mesajı gönderilemedi."
                                ),
                    )

                    webSocket.close(
                        NORMAL_CLOSURE_CODE,
                        "Kimlik doğrulama mesajı gönderilemedi.",
                    )
                }
            }


            /**
             * Sunucudan text JSON mesajı geldiğinde
             * çağrılır.
             */
            override fun onMessage(
                webSocket: WebSocket,
                text: String,
            ) {
                handleIncomingMessage(
                    rawMessage =
                    text,
                )
            }


            /**
             * Binary mesajlar bu uygulamada
             * desteklenmiyor.
             */
            override fun onMessage(
                webSocket: WebSocket,
                bytes: ByteString,
            ) {
                _events.tryEmit(
                    AuthenticatorWebSocketEvent
                        .MessageParsingFailed(
                            rawMessage =
                            bytes.hex(),

                            errorMessage = (
                                    "Binary WebSocket mesajları "
                                            + "desteklenmiyor."
                                    ),
                        ),
                )
            }


            /**
             * Sunucu bağlantıyı kapatmaya başladığında
             * çağrılır.
             */
            override fun onClosing(
                webSocket: WebSocket,
                code: Int,
                reason: String,
            ) {
                stopHeartbeat()

                webSocket.close(
                    code,
                    reason,
                )
            }


            /**
             * Bağlantı tamamen kapandığında çağrılır.
             */
            override fun onClosed(
                webSocket: WebSocket,
                code: Int,
                reason: String,
            ) {
                handleSocketDisconnected(
                    webSocket =
                    webSocket,

                    reason =
                    reason
                        .takeIf {
                            it.isNotBlank()
                        }
                        ?: "WebSocket bağlantısı kapatıldı.",
                )
            }


            /**
             * Bağlantı kurulamazsa veya açık socket
             * üzerinde hata oluşursa çağrılır.
             */
            override fun onFailure(
                webSocket: WebSocket,
                throwable: Throwable,
                response: Response?,
            ) {
                connectionInProgress.set(
                    false,
                )


                stopHeartbeat()


                /*
                 * Eski bir socket callback'i aktif yeni
                 * bağlantıyı etkilemesin.
                 */
                if (
                    activeWebSocket === webSocket
                ) {
                    activeWebSocket =
                        null
                }


                val httpStatus =
                    response
                        ?.code
                        ?.let {
                            " HTTP $it."
                        }
                        .orEmpty()


                val message =
                    throwable.message
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            (
                                    "WebSocket bağlantı hatası: "
                                            + it
                                            + httpStatus
                                    )
                        }
                        ?: (
                                "WebSocket bağlantısında "
                                        + "beklenmeyen hata oluştu."
                                        + httpStatus
                                )


                _connectionState.value =
                    AuthenticatorWebSocketState.Error(
                        message =
                        message,

                        throwable =
                        throwable,
                    )


                if (
                    !manuallyDisconnected.get()
                ) {
                    scheduleReconnect()
                }
            }
        }
    }


    /*
     * =====================================================
     * GELEN MESAJLARI İŞLEME
     * =====================================================
     */


    /**
     * Sunucudan gelen JSON mesajını type alanına göre
     * ilgili modele dönüştürür.
     */
    private fun handleIncomingMessage(
        rawMessage: String,
    ) {
        try {
            val envelope =
                NetworkModule.gson.fromJson(
                    rawMessage,
                    WebSocketMessageEnvelope::class.java,
                )


            when (
                envelope.type
                    ?.trim()
                    ?.lowercase()
            ) {
                WebSocketMessageType.AUTHENTICATED -> {
                    handleAuthenticatedMessage(
                        rawMessage =
                        rawMessage,
                    )
                }


                WebSocketMessageType
                    .AUTHENTICATION_CHALLENGE -> {

                    handleChallengeMessage(
                        rawMessage =
                        rawMessage,
                    )
                }


                WebSocketMessageType
                    .CHALLENGE_CANCELLED -> {

                    handleChallengeCancelledMessage(
                        rawMessage =
                        rawMessage,
                    )
                }


                WebSocketMessageType
                    .CHALLENGE_RESULT -> {

                    handleChallengeResultMessage(
                        rawMessage =
                        rawMessage,
                    )
                }


                WebSocketMessageType.HEARTBEAT_ACK,
                WebSocketMessageType.CONNECTED,
                WebSocketMessageType.DISCONNECT_ACK,
                WebSocketMessageType.ERROR,
                    -> {
                    handleServerMessage(
                        rawMessage =
                        rawMessage,
                    )
                }


                null,
                "",
                    -> {
                    publishParsingError(
                        rawMessage =
                        rawMessage,

                        message = (
                                "WebSocket mesajında type "
                                        + "alanı bulunamadı."
                                ),
                    )
                }


                else -> {
                    publishParsingError(
                        rawMessage =
                        rawMessage,

                        message = (
                                "Desteklenmeyen WebSocket "
                                        + "mesaj türü: "
                                        + envelope.type
                                ),
                    )
                }
            }
        } catch (exception: Exception) {
            publishParsingError(
                rawMessage =
                rawMessage,

                message =
                exception.message
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "WebSocket mesajı çözümlenemedi.",
            )
        }
    }


    /**
     * Başarılı cihaz kimlik doğrulama mesajını işler.
     */
    private fun handleAuthenticatedMessage(
        rawMessage: String,
    ) {
        reconnectAttempt =
            0


        reconnectJob?.cancel()

        reconnectJob =
            null


        _connectionState.value =
            AuthenticatorWebSocketState.Authenticated


        startHeartbeat()


        val serverMessage =
            parseServerMessageSafely(
                rawMessage =
                rawMessage,
            )


        _events.tryEmit(
            AuthenticatorWebSocketEvent
                .ServerMessageReceived(
                    message =
                    serverMessage,
                ),
        )
    }


    /**
     * Yeni authentication challenge mesajını işler.
     */
    private fun handleChallengeMessage(
        rawMessage: String,
    ) {
        val challenge =
            NetworkModule.gson.fromJson(
                rawMessage,
                WebSocketChallengeMessage::class.java,
            )


        when (
            val validationResult =
                challenge.validate()
        ) {
            WebSocketMessageValidationResult.Valid -> {
                _events.tryEmit(
                    AuthenticatorWebSocketEvent
                        .ChallengeReceived(
                            challenge =
                            challenge,
                        ),
                )
            }

            is WebSocketMessageValidationResult.Invalid -> {
                publishParsingError(
                    rawMessage =
                    rawMessage,

                    message =
                    validationResult.message,
                )
            }
        }
    }


    /**
     * Challenge iptal mesajını işler.
     */
    private fun handleChallengeCancelledMessage(
        rawMessage: String,
    ) {
        val cancelledMessage =
            NetworkModule.gson.fromJson(
                rawMessage,
                WebSocketChallengeCancelledMessage::class.java,
            )


        if (
            cancelledMessage
                .challengePublicId
                .trim()
                .isBlank()
        ) {
            publishParsingError(
                rawMessage =
                rawMessage,

                message = (
                        "İptal mesajında challenge "
                                + "kimliği bulunamadı."
                        ),
            )

            return
        }


        _events.tryEmit(
            AuthenticatorWebSocketEvent
                .ChallengeCancelled(
                    message =
                    cancelledMessage,
                ),
        )
    }


    /**
     * Challenge onay veya ret sonucunu işler.
     *
     * Python servisi karar tamamlandıktan sonra açık
     * WebSocket bağlantısına challenge_result mesajı
     * gönderebilir.
     */
    private fun handleChallengeResultMessage(
        rawMessage: String,
    ) {
        val resultMessage =
            NetworkModule.gson.fromJson(
                rawMessage,
                WebSocketChallengeResultMessage::class.java,
            )


        if (
            resultMessage
                .challengePublicId
                .trim()
                .isBlank()
        ) {
            publishParsingError(
                rawMessage =
                rawMessage,

                message =
                "Challenge sonuç kimliği bulunamadı.",
            )

            return
        }


        if (
            resultMessage.status
                .trim()
                .isBlank()
        ) {
            publishParsingError(
                rawMessage =
                rawMessage,

                message =
                "Challenge sonuç durumu bulunamadı.",
            )

            return
        }


        _events.tryEmit(
            AuthenticatorWebSocketEvent
                .ChallengeResultReceived(
                    result =
                    resultMessage,
                ),
        )
    }


    /**
     * Genel sunucu mesajını işler.
     */
    private fun handleServerMessage(
        rawMessage: String,
    ) {
        val serverMessage =
            parseServerMessageSafely(
                rawMessage =
                rawMessage,
            )


        _events.tryEmit(
            AuthenticatorWebSocketEvent
                .ServerMessageReceived(
                    message =
                    serverMessage,
                ),
        )


        if (
            serverMessage.isError()
        ) {
            _connectionState.value =
                AuthenticatorWebSocketState.Error(
                    message =
                    serverMessage.message,
                )
        }
    }


    /**
     * Python sunucu mesajlarını alan farklılıklarına
     * toleranslı biçimde modele dönüştürür.
     */
    private fun parseServerMessageSafely(
        rawMessage: String,
    ): WebSocketServerMessage {
        val jsonObject =
            JsonParser
                .parseString(
                    rawMessage,
                )
                .asJsonObject


        val type =
            jsonObject
                .getStringOrNull(
                    "type",
                )
                ?: "unknown"


        val message =
            jsonObject
                .getStringOrNull(
                    "message",
                )
                ?: when (type) {
                    WebSocketMessageType.AUTHENTICATED -> {
                        "Authenticator cihazı doğrulandı."
                    }

                    WebSocketMessageType.HEARTBEAT_ACK -> {
                        "Heartbeat alındı."
                    }

                    WebSocketMessageType.DISCONNECT_ACK -> {
                        "WebSocket bağlantısı kapatıldı."
                    }

                    WebSocketMessageType.ERROR -> {
                        "WebSocket sunucu hatası."
                    }

                    else -> {
                        "WebSocket sunucu mesajı alındı."
                    }
                }


        val sentAt =
            jsonObject
                .getStringOrNull(
                    "sent_at",
                )
                ?: jsonObject
                    .getStringOrNull(
                        "connected_at",
                    )
                ?: currentUtcIso()


        val code =
            jsonObject
                .getStringOrNull(
                    "code",
                )


        return WebSocketServerMessage(
            type =
            type,

            message =
            message,

            sentAt =
            sentAt,

            code =
            code,
        )
    }


    /*
     * =====================================================
     * HEARTBEAT
     * =====================================================
     */


    /**
     * Belirli aralıklarla Python sunucusuna heartbeat
     * mesajı gönderir.
     */
    private fun startHeartbeat() {
        stopHeartbeat()


        heartbeatJob =
            managerScope.launch {
                while (isActive) {
                    delay(
                        HEARTBEAT_INTERVAL_MILLISECONDS,
                    )


                    if (
                        _connectionState.value
                        != AuthenticatorWebSocketState
                            .Authenticated
                    ) {
                        continue
                    }


                    val sent =
                        sendHeartbeat()


                    if (!sent) {
                        publishConnectionError(
                            message = (
                                    "WebSocket heartbeat "
                                            + "mesajı gönderilemedi."
                                    ),
                        )
                    }
                }
            }
    }


    /**
     * Aktif WebSocket üzerinden heartbeat gönderir.
     */
    fun sendHeartbeat(): Boolean {
        val webSocket =
            activeWebSocket
                ?: return false


        if (
            _connectionState.value
            != AuthenticatorWebSocketState.Authenticated
        ) {
            return false
        }


        val message =
            WebSocketHeartbeatMessage(
                sentAt =
                currentUtcIso(),
            )


        val json =
            NetworkModule.gson.toJson(
                message,
            )


        return webSocket.send(
            json,
        )
    }


    /**
     * Heartbeat coroutine işini durdurur.
     */
    private fun stopHeartbeat() {
        heartbeatJob?.cancel()

        heartbeatJob =
            null
    }


    /*
     * =====================================================
     * BAĞLANTI KAPATMA
     * =====================================================
     */


    /**
     * WebSocket bağlantısını kullanıcı isteğiyle
     * güvenli biçimde kapatır.
     */
    fun disconnect(
        reason: String =
            "Kullanıcı WebSocket bağlantısını kapattı.",
    ) {
        manuallyDisconnected.set(
            true,
        )


        reconnectJob?.cancel()

        reconnectJob =
            null


        stopHeartbeat()


        val webSocket =
            activeWebSocket


        if (webSocket == null) {
            connectionInProgress.set(
                false,
            )

            _connectionState.value =
                AuthenticatorWebSocketState.Disconnected(
                    reason =
                    reason,
                )

            return
        }


        /*
         * Sunucunun beklediği disconnect mesajı model
         * üzerinden JSON'a dönüştürülür.
         */
        val disconnectJson =
            NetworkModule.gson.toJson(
                WebSocketDisconnectMessage(),
            )


        webSocket.send(
            disconnectJson,
        )


        webSocket.close(
            NORMAL_CLOSURE_CODE,
            reason,
        )


        activeWebSocket =
            null


        connectionInProgress.set(
            false,
        )


        _connectionState.value =
            AuthenticatorWebSocketState.Disconnected(
                reason =
                reason,
            )
    }


    /**
     * Socket kapandığında state ve yeniden bağlantı
     * davranışını yönetir.
     */
    private fun handleSocketDisconnected(
        webSocket: WebSocket,
        reason: String,
    ) {
        connectionInProgress.set(
            false,
        )


        stopHeartbeat()


        if (
            activeWebSocket === webSocket
        ) {
            activeWebSocket =
                null
        }


        _connectionState.value =
            AuthenticatorWebSocketState.Disconnected(
                reason =
                reason,
            )


        if (
            !manuallyDisconnected.get()
        ) {
            scheduleReconnect()
        }
    }


    /*
     * =====================================================
     * OTOMATİK YENİDEN BAĞLANTI
     * =====================================================
     */


    /**
     * Beklenmeyen bağlantı kopmalarında artan bekleme
     * süresiyle yeniden bağlantı dener.
     */
    private fun scheduleReconnect() {
        if (
            manuallyDisconnected.get()
        ) {
            return
        }


        if (
            reconnectJob?.isActive == true
        ) {
            return
        }


        reconnectAttempt +=
            1


        val delayMilliseconds =
            calculateReconnectDelay(
                attempt =
                reconnectAttempt,
            )


        reconnectJob =
            managerScope.launch {
                delay(
                    delayMilliseconds,
                )


                if (
                    manuallyDisconnected.get()
                ) {
                    return@launch
                }


                _connectionState.value =
                    AuthenticatorWebSocketState
                        .Disconnected(
                            reason =
                            "Yeniden bağlantı deneniyor.",
                        )


                connectInternal()
            }
    }


    /**
     * Yeniden bağlantı bekleme süresini hesaplar.
     *
     * Denemeler:
     *
     * 1 → 2 saniye
     * 2 → 4 saniye
     * 3 → 8 saniye
     * 4 → 16 saniye
     * 5 ve sonrası → en fazla 30 saniye
     */
    private fun calculateReconnectDelay(
        attempt: Int,
    ): Long {
        val safeAttempt =
            attempt.coerceIn(
                minimumValue =
                1,

                maximumValue =
                5,
            )


        val calculatedDelay =
            INITIAL_RECONNECT_DELAY_MILLISECONDS *
                    (1L shl (safeAttempt - 1))


        return calculatedDelay.coerceAtMost(
            MAX_RECONNECT_DELAY_MILLISECONDS,
        )
    }


    /*
     * =====================================================
     * UTC TARİH OLUŞTURMA
     * =====================================================
     */


    /**
     * API 24 üzerinde çalışabilen UTC ISO-8601 zaman
     * metni oluşturur.
     */
    private fun currentUtcIso(): String {
        val formatter =
            SimpleDateFormat(
                UTC_ISO_DATE_PATTERN,
                Locale.US,
            ).apply {
                timeZone =
                    TimeZone.getTimeZone(
                        "UTC",
                    )
            }


        return formatter.format(
            Date(),
        )
    }


    /*
     * =====================================================
     * HATA YAYINLAMA
     * =====================================================
     */


    /**
     * Bağlantı hatasını state olarak yayınlar.
     */
    private fun publishConnectionError(
        message: String,
        throwable: Throwable? = null,
    ) {
        _connectionState.value =
            AuthenticatorWebSocketState.Error(
                message =
                message,

                throwable =
                throwable,
            )
    }


    /**
     * Mesaj çözümleme hatasını event olarak yayınlar.
     */
    private fun publishParsingError(
        rawMessage: String,
        message: String,
    ) {
        _events.tryEmit(
            AuthenticatorWebSocketEvent
                .MessageParsingFailed(
                    rawMessage =
                    rawMessage,

                    errorMessage =
                    message,
                ),
        )
    }


    /*
     * =====================================================
     * KAYNAKLARI SERBEST BIRAKMA
     * =====================================================
     */


    /**
     * Manager artık kullanılmayacaksa bütün bağlantıları
     * ve coroutine işlerini kapatır.
     */
    fun release() {
        manuallyDisconnected.set(
            true,
        )


        reconnectJob?.cancel()

        reconnectJob =
            null


        stopHeartbeat()


        activeWebSocket?.cancel()

        activeWebSocket =
            null


        connectionInProgress.set(
            false,
        )


        _connectionState.value =
            AuthenticatorWebSocketState.Disconnected(
                reason = (
                        "WebSocket kaynakları serbest "
                                + "bırakıldı."
                        ),
            )


        managerScope.cancel()
    }


    /*
     * =====================================================
     * URL
     * =====================================================
     */


    /**
     * Gerçek Python WebSocket endpoint adresini
     * oluşturur.
     */
    private fun buildWebSocketUrl(): String {
        return (
                NetworkModule
                    .getAuthenticatorWebSocketBaseUrl()
                    .trimEnd('/')
                        + WEBSOCKET_ENDPOINT_PATH
                )
    }


    /*
     * =====================================================
     * SABİTLER
     * =====================================================
     */


    private companion object {

        const val WEBSOCKET_ENDPOINT_PATH =
            "/ws/device"


        const val HEARTBEAT_INTERVAL_MILLISECONDS =
            30_000L


        const val INITIAL_RECONNECT_DELAY_MILLISECONDS =
            2_000L


        const val MAX_RECONNECT_DELAY_MILLISECONDS =
            30_000L


        const val NORMAL_CLOSURE_CODE =
            1000


        const val UTC_ISO_DATE_PATTERN =
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }
}


/*
 * =========================================================
 * JSONOBJECT YARDIMCISI
 * =========================================================
 */


/**
 * JsonObject içerisindeki string alanı güvenli şekilde
 * okur.
 */
private fun JsonObject.getStringOrNull(
    key: String,
): String? {
    val element =
        get(
            key,
        )
            ?: return null


    if (element.isJsonNull) {
        return null
    }


    return try {
        element
            .asString
            .trim()
            .takeIf {
                it.isNotBlank()
            }
    } catch (_: Exception) {
        null
    }
}