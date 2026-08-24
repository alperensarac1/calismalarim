package com.alperensarac.projectmanagementauthenticator.presentation.authenticator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.alperensarac.projectmanagementauthenticator.data.remote.model.AuthenticatorWebSocketEvent
import com.alperensarac.projectmanagementauthenticator.data.remote.model.AuthenticatorWebSocketState
import com.alperensarac.projectmanagementauthenticator.data.remote.model.ChallengeDecisionResult
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketChallengeMessage
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketChallengeResultMessage
import com.alperensarac.projectmanagementauthenticator.data.repository.ChallengeLocationData
import com.alperensarac.projectmanagementauthenticator.data.repository.ChallengeRepository
import com.alperensarac.projectmanagementauthenticator.data.websocket.AuthenticatorWebSocketManager

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


/*
 * =========================================================
 * AUTHENTICATOR EKRAN DURUMU
 * =========================================================
 */


/**
 * Authenticator ana ekranının bütün durumunu temsil eder.
 *
 * Bu state içerisinde:
 *
 * - WebSocket bağlantı durumu
 * - Bekleyen challenge
 * - Onay veya ret işlemi
 * - Kullanıcıya gösterilecek mesajlar
 * - Challenge zaman aşımı bilgisi
 *
 * tek merkezden yönetilir.
 */
data class AuthenticatorUiState(

    /**
     * WebSocket bağlantısının güncel durumudur.
     */
    val connectionState:
    AuthenticatorWebSocketState =
        AuthenticatorWebSocketState.Idle,

    /**
     * WebSocket üzerinden alınan ve henüz işlem
     * yapılmamış challenge mesajıdır.
     */
    val activeChallenge:
    WebSocketChallengeMessage? = null,

    /**
     * Challenge için onay veya ret isteği sunucuya
     * gönderilirken true olur.
     */
    val isDecisionSubmitting: Boolean = false,

    /**
     * Kullanıcının challenge için seçtiği son karar.
     *
     * null:
     * Henüz karar verilmedi.
     *
     * approve:
     * Onay isteği gönderiliyor.
     *
     * reject:
     * Ret isteği gönderiliyor.
     */
    val submittedDecision: String? = null,

    /**
     * Genel bilgi veya başarı mesajıdır.
     */
    val infoMessage: String? = null,

    /**
     * Kullanıcıya gösterilecek hata mesajıdır.
     */
    val errorMessage: String? = null,

    /**
     * Challenge'ın geçerlilik süresinin dolup
     * dolmadığını belirtir.
     */
    val isChallengeExpired: Boolean = false,

    /**
     * Challenge'ın bitmesine kalan saniyedir.
     *
     * Zaman bilgisi çözümlenemiyorsa null olabilir.
     */
    val remainingSeconds: Long? = null,

    /**
     * Kullanıcının verdiği karar başarıyla işlendiğinde
     * true olur.
     *
     * Activity bu olayı kullanarak Snackbar gösterir.
     */
    val isDecisionCompleted: Boolean = false,

    /**
     * Son challenge işlemi başarılı bir onay ise
     * true olur.
     */
    val wasApproved: Boolean = false,

    /**
     * WebSocket bağlantısının kullanıcı tarafından
     * başlatılıp başlatılmadığını belirtir.
     */
    val hasStartedConnection: Boolean = false,
) {

    /**
     * Kullanıcının challenge'ı onaylayıp
     * onaylayamayacağını belirtir.
     */
    val canApprove: Boolean
        get() {
            return (
                    activeChallenge != null &&
                            !isChallengeExpired &&
                            !isDecisionSubmitting
                    )
        }


    /**
     * Kullanıcının challenge'ı reddedip
     * reddedemeyeceğini belirtir.
     *
     * Süresi dolmuş challenge için ret işlemi
     * göndermek gerekli olmayabilir; ancak ekranı
     * kontrollü tutmak adına yalnızca işlem devam
     * etmiyorsa buton etkin kalır.
     */
    val canReject: Boolean
        get() {
            return (
                    activeChallenge != null &&
                            !isDecisionSubmitting
                    )
        }


    /**
     * WebSocket bağlantısının cihaz kimlik doğrulama
     * aşamasını tamamlayıp tamamlamadığını belirtir.
     */
    val isWebSocketAuthenticated: Boolean
        get() {
            return connectionState ==
                    AuthenticatorWebSocketState.Authenticated
        }


    /**
     * Bağlantının kurulmaya veya cihazın doğrulanmaya
     * çalışıldığını belirtir.
     */
    val isConnecting: Boolean
        get() {
            return (
                    connectionState ==
                            AuthenticatorWebSocketState.Connecting ||
                            connectionState ==
                            AuthenticatorWebSocketState.Connected
                    )
        }
}


/*
 * =========================================================
 * AUTHENTICATOR VIEWMODEL
 * =========================================================
 */


/**
 * Authenticator ana ekranındaki WebSocket bağlantısını
 * ve challenge karar akışını yöneten ViewModel'dir.
 *
 * Temel görevleri:
 *
 * 1. WebSocket bağlantısını başlatmak.
 * 2. Bağlantı durumunu ekrana aktarmak.
 * 3. WebSocket challenge olaylarını dinlemek.
 * 4. Challenge onayını Repository üzerinden göndermek.
 * 5. Challenge reddini Repository üzerinden göndermek.
 * 6. Challenge süresini takip etmek.
 * 7. WebSocket challenge_result mesajını işlemek.
 * 8. Hata ve bilgi mesajlarını yönetmek.
 */
class AuthenticatorViewModel(
    private val webSocketManager:
    AuthenticatorWebSocketManager,

    private val challengeRepository:
    ChallengeRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            AuthenticatorUiState(),
        )


    /**
     * Activity tarafından gözlemlenen salt okunur ekran
     * durumudur.
     */
    val uiState:
            StateFlow<AuthenticatorUiState> =
        _uiState.asStateFlow()


    /**
     * Aktif challenge için çalışan geri sayım işidir.
     */
    private var challengeCountdownJob: Job? =
        null


    init {
        observeConnectionState()

        observeWebSocketEvents()
    }


    /*
     * =====================================================
     * WEBSOCKET BAĞLANTISI
     * =====================================================
     */


    /**
     * Python Authenticator WebSocket bağlantısını
     * başlatır.
     */
    fun connect() {
        _uiState.value =
            _uiState.value.copy(
                hasStartedConnection = true,
                errorMessage = null,
                infoMessage =
                "Authenticator sunucusuna bağlanılıyor...",
            )

        webSocketManager.connect()
    }


    /**
     * WebSocket bağlantısını kullanıcı isteğiyle
     * kapatır.
     */
    fun disconnect() {
        stopChallengeCountdown()

        webSocketManager.disconnect(
            reason = (
                    "Authenticator bağlantısı kullanıcı "
                            + "tarafından kapatıldı."
                    ),
        )

        _uiState.value =
            _uiState.value.copy(
                activeChallenge = null,
                isDecisionSubmitting = false,
                submittedDecision = null,
                remainingSeconds = null,
                isChallengeExpired = false,
                infoMessage =
                "Authenticator bağlantısı kapatıldı.",
            )
    }


    /**
     * Bağlantı kopmuşsa yeniden bağlantı başlatır.
     */
    fun reconnect() {
        _uiState.value =
            _uiState.value.copy(
                errorMessage = null,
                infoMessage = (
                        "Authenticator bağlantısı yeniden "
                                + "kuruluyor..."
                        ),
            )

        webSocketManager.connect()
    }


    /**
     * WebSocket bağlantı durumunu gözlemler.
     */
    private fun observeConnectionState() {
        viewModelScope.launch {
            webSocketManager
                .connectionState
                .collect { state ->

                    handleConnectionState(
                        state = state,
                    )
                }
        }
    }


    /**
     * WebSocket bağlantı durumunu UI state'e aktarır.
     */
    private fun handleConnectionState(
        state: AuthenticatorWebSocketState,
    ) {
        when (state) {
            AuthenticatorWebSocketState.Idle -> {
                _uiState.value =
                    _uiState.value.copy(
                        connectionState = state,
                    )
            }


            AuthenticatorWebSocketState.Connecting -> {
                _uiState.value =
                    _uiState.value.copy(
                        connectionState = state,
                        infoMessage =
                        "Authenticator sunucusuna bağlanılıyor...",
                        errorMessage = null,
                    )
            }


            AuthenticatorWebSocketState.Connected -> {
                _uiState.value =
                    _uiState.value.copy(
                        connectionState = state,
                        infoMessage =
                        "Bağlantı kuruldu, cihaz doğrulanıyor...",
                        errorMessage = null,
                    )
            }


            AuthenticatorWebSocketState.Authenticated -> {
                _uiState.value =
                    _uiState.value.copy(
                        connectionState = state,
                        infoMessage = (
                                "Authenticator aktif. Giriş "
                                        + "istekleri bekleniyor."
                                ),
                        errorMessage = null,
                    )
            }


            is AuthenticatorWebSocketState.Disconnected -> {
                _uiState.value =
                    _uiState.value.copy(
                        connectionState = state,
                        infoMessage = null,
                        errorMessage =
                        state.reason
                            ?.trim()
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "WebSocket bağlantısı kapatıldı.",
                    )
            }


            is AuthenticatorWebSocketState.Error -> {
                _uiState.value =
                    _uiState.value.copy(
                        connectionState = state,
                        infoMessage = null,
                        errorMessage =
                        state.message,
                    )
            }
        }
    }


    /*
     * =====================================================
     * WEBSOCKET EVENTLERİ
     * =====================================================
     */


    /**
     * WebSocket manager tarafından yayınlanan olayları
     * dinler.
     */
    private fun observeWebSocketEvents() {
        viewModelScope.launch {
            webSocketManager
                .events
                .collect { event ->

                    handleWebSocketEvent(
                        event = event,
                    )
                }
        }
    }


    /**
     * WebSocket olayını türüne göre işler.
     */
    private fun handleWebSocketEvent(
        event: AuthenticatorWebSocketEvent,
    ) {
        when (event) {
            is AuthenticatorWebSocketEvent
            .ChallengeReceived -> {

                handleChallengeReceived(
                    challenge =
                    event.challenge,
                )
            }


            is AuthenticatorWebSocketEvent
            .ChallengeCancelled -> {

                handleChallengeCancelled(
                    challengePublicId =
                    event.message.challengePublicId,

                    reason =
                    event.message.resolveReason(),
                )
            }


            is AuthenticatorWebSocketEvent
            .ChallengeResultReceived -> {

                handleChallengeResultReceived(
                    result =
                    event.result,
                )
            }


            is AuthenticatorWebSocketEvent
            .ServerMessageReceived -> {

                /*
                 * Heartbeat ACK gibi teknik mesajları
                 * sürekli kullanıcıya göstermiyoruz.
                 *
                 * Yalnızca hata mesajlarını ekrana
                 * yansıtıyoruz.
                 */
                if (event.message.isError()) {
                    _uiState.value =
                        _uiState.value.copy(
                            errorMessage =
                            event.message.message,

                            infoMessage =
                            null,
                        )
                }
            }


            is AuthenticatorWebSocketEvent
            .MessageParsingFailed -> {

                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                        event.errorMessage,

                        infoMessage =
                        null,
                    )
            }
        }
    }


    /*
     * =====================================================
     * YENİ CHALLENGE
     * =====================================================
     */


    /**
     * Yeni challenge geldiğinde ekrana aktarır ve geri
     * sayımı başlatır.
     */
    private fun handleChallengeReceived(
        challenge: WebSocketChallengeMessage,
    ) {
        stopChallengeCountdown()


        _uiState.value =
            _uiState.value.copy(
                activeChallenge =
                challenge,

                isDecisionSubmitting =
                false,

                submittedDecision =
                null,

                isChallengeExpired =
                false,

                remainingSeconds =
                null,

                isDecisionCompleted =
                false,

                wasApproved =
                false,

                errorMessage =
                null,

                infoMessage =
                "Yeni bir giriş doğrulama isteği alındı.",
            )


        startChallengeCountdown(
            challenge =
            challenge,
        )
    }


    /*
     * =====================================================
     * CHALLENGE İPTALİ
     * =====================================================
     */


    /**
     * Sunucu challenge'ın iptal edildiğini bildirdiğinde
     * ekrandaki aynı challenge'ı temizler.
     */
    private fun handleChallengeCancelled(
        challengePublicId: String,
        reason: String,
    ) {
        val currentChallenge =
            _uiState.value.activeChallenge
                ?: return


        if (
            !currentChallenge
                .challengePublicId
                .equals(
                    other =
                    challengePublicId,

                    ignoreCase =
                    true,
                )
        ) {
            return
        }


        stopChallengeCountdown()


        _uiState.value =
            _uiState.value.copy(
                activeChallenge = null,
                isDecisionSubmitting = false,
                submittedDecision = null,
                remainingSeconds = null,
                isChallengeExpired = false,
                isDecisionCompleted = false,
                wasApproved = false,
                infoMessage = reason,
                errorMessage = null,
            )
    }


    /*
     * =====================================================
     * WEBSOCKET CHALLENGE SONUCU
     * =====================================================
     */


    /**
     * Python servisinden WebSocket üzerinden gelen
     * challenge_result mesajını işler.
     *
     * Bu mesaj şu durumlarda gelebilir:
     *
     * - Mobil uygulama challenge'ı onayladığında
     * - Mobil uygulama challenge'ı reddettiğinde
     * - Challenge başka bir istemci tarafından
     *   tamamlandığında
     * - Challenge süresi dolduğunda
     *
     * Aynı sonuç HTTP cevabıyla da gelebileceği için
     * tekrar eden UI olaylarına karşı kontrollü işlem
     * yapılır.
     */
    private fun handleChallengeResultReceived(
        result: WebSocketChallengeResultMessage,
    ) {
        val currentState =
            _uiState.value

        val activeChallenge =
            currentState.activeChallenge


        /*
         * Aktif challenge varsa gelen sonucun aynı
         * challenge'a ait olup olmadığını kontrol ederiz.
         */
        val matchesActiveChallenge =
            activeChallenge
                ?.challengePublicId
                ?.equals(
                    other =
                    result.challengePublicId,

                    ignoreCase =
                    true,
                )
                ?: false


        /*
         * Ekranda başka bir challenge bulunuyorsa eski
         * challenge sonucunun yeni challenge'ı
         * etkilemesine izin vermiyoruz.
         */
        if (
            activeChallenge != null &&
            !matchesActiveChallenge
        ) {
            return
        }


        /*
         * HTTP sonucu daha önce işlendi ve challenge
         * ekrandan kaldırıldıysa aynı WebSocket sonucunu
         * ikinci kez Snackbar olayı olarak yayınlamayız.
         */
        if (
            activeChallenge == null &&
            currentState.isDecisionCompleted
        ) {
            return
        }


        stopChallengeCountdown()


        val approved =
            result.isApproved()


        val expired =
            result.status.equals(
                other =
                "expired",

                ignoreCase =
                true,
            )


        _uiState.value =
            currentState.copy(
                activeChallenge =
                null,

                isDecisionSubmitting =
                false,

                submittedDecision =
                null,

                remainingSeconds =
                if (expired) {
                    0L
                } else {
                    null
                },

                isChallengeExpired =
                expired,

                /*
                 * Süre dolması başarılı bir kullanıcı
                 * kararı değildir. Bu nedenle Snackbar
                 * tamamlanma olayını yalnızca onay veya
                 * ret sonucu için etkinleştiriyoruz.
                 */
                isDecisionCompleted =
                !expired,

                wasApproved =
                approved,

                infoMessage =
                if (expired) {
                    null
                } else {
                    result.resolveMessage()
                },

                errorMessage =
                if (expired) {
                    result.resolveMessage()
                } else {
                    null
                },
            )
    }


    /*
     * =====================================================
     * CHALLENGE ONAY VE RET
     * =====================================================
     */


    /**
     * Aktif challenge'ı onaylar.
     */
    fun approveActiveChallenge(
        location: ChallengeLocationData? = null,
    ) {
        submitActiveChallengeDecision(
            approve =
            true,

            location =
            location,
        )
    }


    /**
     * Aktif challenge'ı reddeder.
     *
     * Activity cihazdan konum alabildiyse bu bilgi
     * Repository katmanına aktarılır.
     */
    fun rejectActiveChallenge(
        location: ChallengeLocationData? = null,
    ) {
        submitActiveChallengeDecision(
            approve =
            false,

            location =
            location,
        )
    }


    /**
     * Aktif challenge için onay veya ret kararını
     * Repository katmanına gönderir.
     *
     * Konum bilgisi Activity tarafından sağlanmışsa
     * request içerisinde Python servisine iletilir.
     */
    private fun submitActiveChallengeDecision(
        approve: Boolean,
        location: ChallengeLocationData? = null,
    ) {
        val currentState =
            _uiState.value


        val challenge =
            currentState.activeChallenge
                ?: run {
                    _uiState.value =
                        currentState.copy(
                            errorMessage = (
                                    "İşlem yapılacak aktif "
                                            + "doğrulama isteği "
                                            + "bulunamadı."
                                    ),
                        )

                    return
                }


        /*
         * Aynı kararın art arda gönderilmesini engeller.
         */
        if (
            currentState.isDecisionSubmitting
        ) {
            return
        }


        /*
         * Süresi dolmuş challenge onaylanamaz.
         */
        if (
            approve &&
            currentState.isChallengeExpired
        ) {
            _uiState.value =
                currentState.copy(
                    errorMessage = (
                            "Süresi dolmuş doğrulama isteği "
                                    + "onaylanamaz."
                            ),
                )

            return
        }


        viewModelScope.launch {
            val decisionText =
                if (approve) {
                    "approve"
                } else {
                    "reject"
                }


            _uiState.value =
                _uiState.value.copy(
                    isDecisionSubmitting =
                    true,

                    submittedDecision =
                    decisionText,

                    errorMessage =
                    null,

                    infoMessage =
                    if (approve) {
                        "Giriş isteği onaylanıyor..."
                    } else {
                        "Giriş isteği reddediliyor..."
                    },
                )


            val result =
                if (approve) {
                    challengeRepository
                        .approveChallenge(
                            challenge =
                            challenge,

                            location =
                            location,
                        )
                } else {
                    challengeRepository
                        .rejectChallenge(
                            challenge =
                            challenge,

                            location =
                            location,
                        )
                }


            handleChallengeDecisionResult(
                result =
                result,

                processedChallengeId =
                challenge.challengePublicId,
            )
        }
    }


    /**
     * Repository tarafından dönen HTTP challenge karar
     * sonucunu ekrana aktarır.
     */
    private fun handleChallengeDecisionResult(
        result: ChallengeDecisionResult,
        processedChallengeId: String,
    ) {
        when (result) {
            is ChallengeDecisionResult.Success -> {
                stopChallengeCountdown()


                val currentState =
                    _uiState.value


                val currentChallenge =
                    currentState.activeChallenge


                /*
                 * WebSocket challenge_result mesajı HTTP
                 * cevabından önce geldiyse aktif challenge
                 * zaten temizlenmiş olabilir.
                 *
                 * Aynı başarılı sonucu ikinci kez event
                 * olarak yayınlamıyoruz.
                 */
                if (
                    currentChallenge == null &&
                    currentState.isDecisionCompleted
                ) {
                    return
                }


                /*
                 * İşlem devam ederken yeni bir challenge
                 * geldiyse yeni challenge'ı yanlışlıkla
                 * temizlemiyoruz.
                 */
                val shouldClearChallenge =
                    currentChallenge
                        ?.challengePublicId
                        ?.equals(
                            other =
                            processedChallengeId,

                            ignoreCase =
                            true,
                        )
                        ?: false


                _uiState.value =
                    currentState.copy(
                        activeChallenge =
                        if (shouldClearChallenge) {
                            null
                        } else {
                            currentChallenge
                        },

                        isDecisionSubmitting =
                        false,

                        submittedDecision =
                        null,

                        remainingSeconds =
                        null,

                        isChallengeExpired =
                        false,

                        isDecisionCompleted =
                        true,

                        wasApproved =
                        result.verification.isApproved(),

                        infoMessage =
                        result.message
                            ?.trim()
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: result.verification
                                .resolveResultMessage(),

                        errorMessage =
                        null,
                    )
            }


            is ChallengeDecisionResult.Failure -> {
                /*
                 * WebSocket sonucu başarılı biçimde
                 * işlendiyse daha sonra gelen HTTP hata
                 * cevabıyla başarı ekranını bozmuyoruz.
                 */
                if (
                    _uiState.value.isDecisionCompleted
                ) {
                    return
                }


                _uiState.value =
                    _uiState.value.copy(
                        isDecisionSubmitting =
                        false,

                        submittedDecision =
                        null,

                        isDecisionCompleted =
                        false,

                        wasApproved =
                        false,

                        infoMessage =
                        null,

                        errorMessage =
                        result.message,
                    )
            }
        }
    }


    /*
     * =====================================================
     * CHALLENGE GERİ SAYIMI
     * =====================================================
     */


    /**
     * Challenge'ın expires_at alanına göre her saniye
     * kalan süreyi günceller.
     */
    private fun startChallengeCountdown(
        challenge: WebSocketChallengeMessage,
    ) {
        stopChallengeCountdown()


        challengeCountdownJob =
            viewModelScope.launch {
                while (true) {
                    /*
                     * Bu coroutine çalışırken yeni bir
                     * challenge geldiyse eski challenge'ın
                     * geri sayımının state'i değiştirmesini
                     * engelleriz.
                     */
                    val activeChallengeId =
                        _uiState.value
                            .activeChallenge
                            ?.challengePublicId


                    if (
                        !activeChallengeId.equals(
                            other =
                            challenge.challengePublicId,

                            ignoreCase =
                            true,
                        )
                    ) {
                        return@launch
                    }


                    val remaining =
                        calculateRemainingSeconds(
                            expiresAt =
                            challenge.expiresAt,
                        )


                    if (remaining == null) {
                        _uiState.value =
                            _uiState.value.copy(
                                remainingSeconds =
                                null,

                                errorMessage = (
                                        "Challenge süre bilgisi "
                                                + "okunamadı."
                                        ),
                            )

                        return@launch
                    }


                    if (remaining <= 0L) {
                        _uiState.value =
                            _uiState.value.copy(
                                remainingSeconds =
                                0L,

                                isChallengeExpired =
                                true,

                                infoMessage =
                                null,

                                errorMessage = (
                                        "Doğrulama isteğinin "
                                                + "süresi doldu."
                                        ),
                            )

                        return@launch
                    }


                    _uiState.value =
                        _uiState.value.copy(
                            remainingSeconds =
                            remaining,

                            isChallengeExpired =
                            false,
                        )


                    delay(
                        1_000L,
                    )
                }
            }
    }


    /**
     * Çalışan challenge geri sayımını durdurur.
     */
    private fun stopChallengeCountdown() {
        challengeCountdownJob?.cancel()

        challengeCountdownJob =
            null
    }


    /**
     * ISO-8601 expires_at değerinden kalan saniyeyi
     * hesaplar.
     *
     * java.time yerine SimpleDateFormat kullanıldığı
     * için minimum SDK 24 üzerinde çalışır.
     */
    private fun calculateRemainingSeconds(
        expiresAt: String,
    ): Long? {
        val expirationTimeMillis =
            parseIsoDateTimeToMillis(
                value =
                expiresAt,
            )
                ?: return null


        val differenceMilliseconds =
            expirationTimeMillis -
                    System.currentTimeMillis()


        /*
         * Tam olmayan son saniyeyi hemen sıfıra
         * düşürmemek için yukarı yuvarlarız.
         */
        return if (
            differenceMilliseconds <= 0L
        ) {
            0L
        } else {
            (
                    differenceMilliseconds +
                            999L
                    ) / 1_000L
        }
    }


    /**
     * Python servisinden gelen ISO-8601 zaman metnini
     * Unix milisaniye değerine dönüştürür.
     *
     * Desteklenen örnekler:
     *
     * 2026-08-03T10:15:00Z
     * 2026-08-03T10:15:00+00:00
     * 2026-08-03T13:15:00+03:00
     * 2026-08-03T10:15:00.123456+00:00
     */
    private fun parseIsoDateTimeToMillis(
        value: String,
    ): Long? {
        val normalizedValue =
            normalizeIsoDateTime(
                value =
                value,
            )
                ?: return null


        val formatPatterns =
            listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
            )


        formatPatterns.forEach { pattern ->
            try {
                val formatter =
                    SimpleDateFormat(
                        pattern,
                        Locale.US,
                    ).apply {
                        isLenient =
                            false

                        timeZone =
                            TimeZone.getTimeZone(
                                "UTC",
                            )
                    }


                val parsedDate =
                    formatter.parse(
                        normalizedValue,
                    )


                if (parsedDate != null) {
                    return parsedDate.time
                }
            } catch (_: Exception) {
                /*
                 * Bu format başarısız olduysa sonraki
                 * format denenir.
                 */
            }
        }


        return null
    }


    /**
     * Python datetime.isoformat() çıktısındaki altı
     * basamaklı mikrosaniyeyi Android'in üç basamaklı
     * milisaniye biçimine indirger.
     *
     * Örnek:
     *
     * 2026-08-03T10:15:00.123456+00:00
     *
     * şu hâle gelir:
     *
     * 2026-08-03T10:15:00.123+00:00
     */
    private fun normalizeIsoDateTime(
        value: String,
    ): String? {
        var normalizedValue =
            value.trim()


        if (
            normalizedValue.isBlank()
        ) {
            return null
        }


        /*
         * Küçük z ile gelen UTC değerini büyük Z
         * biçimine çeviririz.
         */
        if (
            normalizedValue.endsWith(
                suffix =
                "z",
            )
        ) {
            normalizedValue =
                normalizedValue.dropLast(
                    1,
                ) + "Z"
        }


        /*
         * Kesirli saniye bölümünü bulur ve en fazla üç
         * basamağa indirir.
         */
        val fractionRegex =
            Regex(
                pattern =
                """\.(\d+)(?=Z|[+-]\d{2}:\d{2}$)""",
            )


        normalizedValue =
            fractionRegex.replace(
                input =
                normalizedValue,
            ) { matchResult ->

                val digits =
                    matchResult
                        .groupValues[1]


                val milliseconds =
                    digits
                        .take(
                            3,
                        )
                        .padEnd(
                            length =
                            3,

                            padChar =
                            '0',
                        )


                ".$milliseconds"
            }


        return normalizedValue
    }


    /*
     * =====================================================
     * UI EVENT TEMİZLEME
     * =====================================================
     */


    /**
     * Kullanıcı hata mesajını gördükten sonra temizler.
     */
    fun clearErrorMessage() {
        _uiState.value =
            _uiState.value.copy(
                errorMessage =
                null,
            )
    }


    /**
     * Bilgi mesajını temizler.
     */
    fun clearInfoMessage() {
        _uiState.value =
            _uiState.value.copy(
                infoMessage =
                null,
            )
    }


    /**
     * Başarılı challenge karar olayını tüketir.
     *
     * Activity yeniden oluşturulduğunda aynı Snackbar'ın
     * yeniden gösterilmesini önler.
     */
    fun consumeDecisionCompletedEvent() {
        _uiState.value =
            _uiState.value.copy(
                isDecisionCompleted =
                false,
            )
    }


    /**
     * Challenge kartını ekrandan kaldırır.
     *
     * Bu işlem sunucuya onay veya ret göndermez.
     */
    fun dismissActiveChallenge() {
        stopChallengeCountdown()


        _uiState.value =
            _uiState.value.copy(
                activeChallenge =
                null,

                isDecisionSubmitting =
                false,

                submittedDecision =
                null,

                remainingSeconds =
                null,

                isChallengeExpired =
                false,

                isDecisionCompleted =
                false,
            )
    }


    /*
     * =====================================================
     * VIEWMODEL TEMİZLENMESİ
     * =====================================================
     */


    override fun onCleared() {
        stopChallengeCountdown()


        /*
         * WebSocket manager bu ViewModel'e ait olduğu
         * için ViewModel yok edildiğinde bağlantı ve
         * coroutine kaynakları serbest bırakılır.
         */
        webSocketManager.release()


        super.onCleared()
    }
}


/*
 * =========================================================
 * VIEWMODEL FACTORY
 * =========================================================
 */


/**
 * AuthenticatorViewModel constructor parametreleri aldığı
 * için kullanılan özel ViewModel Factory sınıfıdır.
 */
class AuthenticatorViewModelFactory(
    private val webSocketManager:
    AuthenticatorWebSocketManager,

    private val challengeRepository:
    ChallengeRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (
            modelClass.isAssignableFrom(
                AuthenticatorViewModel::class.java,
            )
        ) {
            return AuthenticatorViewModel(
                webSocketManager =
                webSocketManager,

                challengeRepository =
                challengeRepository,
            ) as T
        }


        throw IllegalArgumentException(
            "Bilinmeyen ViewModel sınıfı: "
                    + modelClass.name,
        )
    }
}