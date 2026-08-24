package com.alperensarac.projectmanagementauthenticator.presentation.authenticator

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.alperensarac.projectmanagementauthenticator.data.local.AuthSessionManager
import com.alperensarac.projectmanagementauthenticator.data.remote.model.AuthenticatorWebSocketState
import com.alperensarac.projectmanagementauthenticator.data.remote.model.LocationPermissionStatus
import com.alperensarac.projectmanagementauthenticator.data.remote.model.WebSocketChallengeMessage
import com.alperensarac.projectmanagementauthenticator.data.repository.ChallengeLocationData
import com.alperensarac.projectmanagementauthenticator.data.repository.ChallengeRepository
import com.alperensarac.projectmanagementauthenticator.data.websocket.AuthenticatorWebSocketManager
import com.alperensarac.projectmanagementauthenticator.databinding.ActivityAuthenticatorBinding

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


/*
 * =========================================================
 * AUTHENTICATOR ACTIVITY
 * =========================================================
 */


/**
 * Mobil Authenticator uygulamasının ana ekranıdır.
 *
 * Bu ekranın temel görevleri:
 *
 * - Python WebSocket servisine bağlanmak
 * - Bağlantı durumunu göstermek
 * - Yeni giriş doğrulama isteğini göstermek
 * - Kullanıcının isteği onaylamasını sağlamak
 * - Kullanıcının isteği reddetmesini sağlamak
 * - Challenge süresini göstermek
 * - Bağlantı koptuğunda yeniden bağlanmayı sağlamak
 *
 * Activity doğrudan Retrofit veya Android Keystore ile
 * iletişim kurmaz.
 *
 * Katman akışı:
 *
 * AuthenticatorActivity
 *      ↓
 * AuthenticatorViewModel
 *      ↓
 * ChallengeRepository
 *      ↓
 * Retrofit + Android Keystore
 *
 * WebSocket akışı:
 *
 * AuthenticatorActivity
 *      ↓
 * AuthenticatorViewModel
 *      ↓
 * AuthenticatorWebSocketManager
 *      ↓
 * Python /ws/device
 */
class AuthenticatorActivity : AppCompatActivity() {

    /*
     * =====================================================
     * VIEW BINDING
     * =====================================================
     */


    /**
     * activity_authenticator.xml içindeki bileşenlere
     * güvenli erişim sağlar.
     */
    private lateinit var binding:
            ActivityAuthenticatorBinding


    /*
     * =====================================================
     * BAĞIMLILIKLAR
     * =====================================================
     */


    /**
     * DataStore tabanlı oturum yöneticisi.
     */
    private val authSessionManager:
            AuthSessionManager by lazy {

        AuthSessionManager(
            context = applicationContext,
        )
    }


    /**
     * Python WebSocket bağlantısını yöneten sınıf.
     */
    private val webSocketManager:
            AuthenticatorWebSocketManager by lazy {

        AuthenticatorWebSocketManager(
            authSessionManager =
            authSessionManager,
        )
    }


    /**
     * Challenge onay ve ret HTTP işlemlerini yöneten
     * Repository.
     */
    private val challengeRepository:
            ChallengeRepository by lazy {

        ChallengeRepository(
            authSessionManager =
            authSessionManager,
        )
    }


    /**
     * Authenticator ekranının ViewModel'i.
     */
    private val authenticatorViewModel:
            AuthenticatorViewModel by viewModels {

        AuthenticatorViewModelFactory(
            webSocketManager =
            webSocketManager,

            challengeRepository =
            challengeRepository,
        )
    }


    /*
     * =====================================================
     * KONUM BAĞIMLILIKLARI
     * =====================================================
     */


    /**
     * Kullanıcı izin verirken bekleyen karar:
     *
     * true  = onay
     * false = ret
     * null  = bekleyen karar yok
     */
    private var pendingApproveDecision: Boolean? =
        null


    /**
     * Aktif tek seferlik konum dinleyicisi.
     */
    private var activeLocationListener:
            LocationListener? = null


    /**
     * Konum isteğinin zaman aşımı görevi.
     */
    private var activeLocationTimeout:
            Runnable? = null


    private val locationHandler =
        Handler(
            Looper.getMainLooper(),
        )


    /**
     * Android çalışma zamanı konum iznini ister.
     */
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .RequestMultiplePermissions(),
        ) {
                permissions ->

            val approve =
                pendingApproveDecision
                    ?: return@registerForActivityResult


            pendingApproveDecision =
                null


            val fineGranted =
                permissions[
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ] == true ||
                        hasFineLocationPermission()


            val coarseGranted =
                permissions[
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                ] == true ||
                        hasCoarseLocationPermission()


            if (
                fineGranted ||
                coarseGranted
            ) {
                captureLocationAndSubmitDecision(
                    approve =
                    approve,
                )
            } else {
                submitDecision(
                    approve =
                    approve,

                    location =
                    ChallengeLocationData(
                        permissionStatus =
                        LocationPermissionStatus.DENIED,
                    ),
                )
            }
        }


    /*
     * =====================================================
     * ACTIVITY YAŞAM DÖNGÜSÜ
     * =====================================================
     */


    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(
            savedInstanceState,
        )


        binding =
            ActivityAuthenticatorBinding.inflate(
                layoutInflater,
            )


        setContentView(
            binding.root,
        )


        setupButtonListeners()

        setupBackPressHandling()

        observeUiState()


        /*
         * Activity ilk kez açıldığında WebSocket
         * bağlantısını başlatıyoruz.
         *
         * ViewModel manager üzerinden aynı anda ikinci
         * bağlantı kurulmasını engeller.
         */
        if (
            savedInstanceState == null
        ) {
            authenticatorViewModel.connect()
        }
    }


    /*
     * =====================================================
     * BUTON İŞLEMLERİ
     * =====================================================
     */


    /**
     * Ekrandaki bütün buton click listenerlarını bağlar.
     */
    private fun setupButtonListeners() {
        binding.reconnectButton
            .setOnClickListener {

                authenticatorViewModel.reconnect()
            }


        binding.approveChallengeButton
            .setOnClickListener {

                showApproveConfirmationDialog()
            }


        binding.rejectChallengeButton
            .setOnClickListener {

                showRejectConfirmationDialog()
            }


        binding.dismissChallengeButton
            .setOnClickListener {

                showDismissChallengeDialog()
            }
    }


    /*
     * =====================================================
     * STATE GÖZLEMLEME
     * =====================================================
     */


    /**
     * AuthenticatorUiState akışını lifecycle-aware
     * biçimde gözlemler.
     */
    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                authenticatorViewModel
                    .uiState
                    .collect {
                            uiState ->

                        renderUiState(
                            uiState =
                            uiState,
                        )
                    }
            }
        }
    }


    /*
     * =====================================================
     * EKRAN ÇİZİMİ
     * =====================================================
     */


    /**
     * Güncel ViewModel state'ini XML bileşenlerine
     * aktarır.
     */
    private fun renderUiState(
        uiState: AuthenticatorUiState,
    ) {
        renderConnectionState(
            state =
            uiState.connectionState,
        )


        renderMessages(
            infoMessage =
            uiState.infoMessage,

            errorMessage =
            uiState.errorMessage,
        )


        renderChallenge(
            challenge =
            uiState.activeChallenge,

            remainingSeconds =
            uiState.remainingSeconds,

            isExpired =
            uiState.isChallengeExpired,

            isDecisionSubmitting =
            uiState.isDecisionSubmitting,

            submittedDecision =
            uiState.submittedDecision,

            canApprove =
            uiState.canApprove,

            canReject =
            uiState.canReject,
        )


        /*
         * Bağlantı kesilmişse veya hata varsa yeniden
         * bağlan butonu gösterilir.
         */
        binding.reconnectButton.isVisible =
            when (uiState.connectionState) {
                is AuthenticatorWebSocketState.Disconnected,
                is AuthenticatorWebSocketState.Error,
                    -> true

                else -> false
            }
        binding.reconnectButton.isEnabled =
            !uiState.isConnecting


        /*
         * Karar tamamlandı olayı bir kez işlenir.
         */
        if (
            uiState.isDecisionCompleted
        ) {
            handleDecisionCompleted(
                uiState =
                uiState,
            )
        }
    }


    /*
     * =====================================================
     * BAĞLANTI DURUMU
     * =====================================================
     */


    /**
     * WebSocket bağlantı durumunu üst karta yansıtır.
     */
    private fun renderConnectionState(
        state: AuthenticatorWebSocketState,
    ) {
        when (state) {
            AuthenticatorWebSocketState.Idle -> {
                binding.connectionStatusTitleTextView.text =
                    "Bağlantı bekleniyor"

                binding.connectionStatusDescriptionTextView.text =
                    "Python Authenticator sunucusuna henüz bağlanılmadı."

                binding.connectionStatusIconImageView
                    .setImageResource(
                        android.R.drawable.presence_invisible,
                    )

                binding.connectionProgressIndicator
                    .isVisible =
                    false
            }


            AuthenticatorWebSocketState.Connecting -> {
                binding.connectionStatusTitleTextView.text =
                    "Bağlanılıyor"

                binding.connectionStatusDescriptionTextView.text =
                    "Python Authenticator sunucusuna bağlantı kuruluyor."

                binding.connectionStatusIconImageView
                    .setImageResource(
                        android.R.drawable.presence_away,
                    )

                binding.connectionProgressIndicator
                    .isVisible =
                    true
            }


            AuthenticatorWebSocketState.Connected -> {
                binding.connectionStatusTitleTextView.text =
                    "Cihaz doğrulanıyor"

                binding.connectionStatusDescriptionTextView.text =
                    "WebSocket açıldı, cihaz oturumu kontrol ediliyor."

                binding.connectionStatusIconImageView
                    .setImageResource(
                        android.R.drawable.presence_away,
                    )

                binding.connectionProgressIndicator
                    .isVisible =
                    true
            }


            AuthenticatorWebSocketState.Authenticated -> {
                binding.connectionStatusTitleTextView.text =
                    "Authenticator aktif"

                binding.connectionStatusDescriptionTextView.text =
                    "Giriş doğrulama istekleri bekleniyor."

                binding.connectionStatusIconImageView
                    .setImageResource(
                        android.R.drawable.presence_online,
                    )

                binding.connectionProgressIndicator
                    .isVisible =
                    false
            }


            is AuthenticatorWebSocketState.Disconnected -> {
                binding.connectionStatusTitleTextView.text =
                    "Bağlantı kapalı"

                binding.connectionStatusDescriptionTextView.text =
                    state.reason
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "WebSocket bağlantısı kapatıldı."

                binding.connectionStatusIconImageView
                    .setImageResource(
                        android.R.drawable.presence_offline,
                    )

                binding.connectionProgressIndicator
                    .isVisible =
                    false
            }


            is AuthenticatorWebSocketState.Error -> {
                binding.connectionStatusTitleTextView.text =
                    "Bağlantı hatası"

                binding.connectionStatusDescriptionTextView.text =
                    state.message

                binding.connectionStatusIconImageView
                    .setImageResource(
                        android.R.drawable.stat_notify_error,
                    )

                binding.connectionProgressIndicator
                    .isVisible =
                    false
            }
        }
    }


    /*
     * =====================================================
     * MESAJLAR
     * =====================================================
     */


    /**
     * Bilgi ve hata mesajlarını ekrana yansıtır.
     */
    private fun renderMessages(
        infoMessage: String?,
        errorMessage: String?,
    ) {
        val normalizedInfoMessage =
            infoMessage
                ?.trim()
                .orEmpty()


        binding.infoMessageTextView.isVisible =
            normalizedInfoMessage.isNotBlank()

        binding.infoMessageTextView.text =
            normalizedInfoMessage


        val normalizedErrorMessage =
            errorMessage
                ?.trim()
                .orEmpty()


        binding.errorMessageTextView.isVisible =
            normalizedErrorMessage.isNotBlank()

        binding.errorMessageTextView.text =
            normalizedErrorMessage
    }


    /*
     * =====================================================
     * CHALLENGE EKRANI
     * =====================================================
     */


    /**
     * Aktif challenge varsa challenge kartını, yoksa
     * bekleme kartını gösterir.
     */
    private fun renderChallenge(
        challenge: WebSocketChallengeMessage?,
        remainingSeconds: Long?,
        isExpired: Boolean,
        isDecisionSubmitting: Boolean,
        submittedDecision: String?,
        canApprove: Boolean,
        canReject: Boolean,
    ) {
        val hasActiveChallenge =
            challenge != null


        binding.challengeCard.isVisible =
            hasActiveChallenge

        binding.waitingCard.isVisible =
            !hasActiveChallenge


        /*
         * Aktif challenge yoksa diğer alanları
         * güncellemeye gerek yoktur.
         */
        if (challenge == null) {
            return
        }


        binding.challengeUserNameTextView.text =
            challenge.resolveUserDisplayName()


        val email =
            challenge.email
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }


        binding.challengeEmailTextView.isVisible =
            email != null

        binding.challengeEmailTextView.text =
            email.orEmpty()


        binding.challengeOriginTextView.text =
            challenge.resolveRequestOrigin()


        binding.challengeIpTextView.text =
            challenge.requestIp
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Bilinmiyor"


        renderOneTimeCode(
            challenge =
            challenge,
        )


        renderCountdown(
            remainingSeconds =
            remainingSeconds,

            isExpired =
            isExpired,
        )


        renderDecisionLoading(
            isSubmitting =
            isDecisionSubmitting,

            submittedDecision =
            submittedDecision,
        )


        binding.approveChallengeButton.isEnabled =
            canApprove

        binding.rejectChallengeButton.isEnabled =
            canReject

        binding.dismissChallengeButton.isEnabled =
            !isDecisionSubmitting
    }


    /**
     * Challenge içerisinde tek kullanımlık kod varsa
     * ilgili kartı gösterir.
     */
    private fun renderOneTimeCode(
        challenge: WebSocketChallengeMessage,
    ) {
        val oneTimeCode =
            challenge.oneTimeCode
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }


        binding.oneTimeCodeCard.isVisible =
            oneTimeCode != null


        /*
         * Demo sürecinde backend tarafından one_time_code
         * gönderildiğinde kod doğrudan gösterilir.
         *
         * Beklenen test kodu:
         *
         * 987456
         */
        binding.oneTimeCodeTextView.text =
            oneTimeCode.orEmpty()
    }


    /**
     * Challenge geri sayımını ekrana yansıtır.
     */
    private fun renderCountdown(
        remainingSeconds: Long?,
        isExpired: Boolean,
    ) {
        binding.countdownTextView.text =
            when {
                isExpired -> {
                    "Doğrulama isteğinin süresi doldu"
                }

                remainingSeconds == null -> {
                    "Kalan süre: --"
                }

                remainingSeconds <= 0L -> {
                    "Doğrulama isteğinin süresi doldu"
                }

                else -> {
                    "Kalan süre: $remainingSeconds saniye"
                }
            }


        binding.countdownIconImageView
            .setImageResource(
                if (isExpired) {
                    android.R.drawable.ic_delete
                } else {
                    android.R.drawable.ic_lock_idle_alarm
                },
            )
    }


    /**
     * Onay veya ret isteği gönderilirken progress
     * alanını gösterir.
     */
    private fun renderDecisionLoading(
        isSubmitting: Boolean,
        submittedDecision: String?,
    ) {
        binding.decisionLoadingContainer.isVisible =
            isSubmitting


        binding.decisionLoadingTextView.text =
            when (submittedDecision) {
                "approve" -> {
                    "Giriş isteği onaylanıyor..."
                }

                "reject" -> {
                    "Giriş isteği reddediliyor..."
                }

                else -> {
                    "Karar gönderiliyor..."
                }
            }
    }


    /*
     * =====================================================
     * ONAY DİYALOĞU
     * =====================================================
     */


    /**
     * Kullanıcı onay butonuna bastığında yanlışlıkla
     * onay vermesini azaltmak için doğrulama diyaloğu
     * gösterir.
     */
    private fun showApproveConfirmationDialog() {
        val challenge =
            authenticatorViewModel
                .uiState
                .value
                .activeChallenge
                ?: return


        val userName =
            challenge.resolveUserDisplayName()


        MaterialAlertDialogBuilder(
            this,
        )
            .setTitle(
                "Giriş isteğini onayla",
            )
            .setMessage(
                "$userName kullanıcısı için gelen giriş isteğini onaylamak istiyor musunuz?",
            )
            .setNegativeButton(
                "Vazgeç",
                null,
            )
            .setPositiveButton(
                "Onayla",
            ) {
                    _,
                    _ ->

                requestLocationAndSubmitDecision(
                    approve =
                    true,
                )
            }
            .show()
    }


    /*
     * =====================================================
     * RET DİYALOĞU
     * =====================================================
     */


    /**
     * Kullanıcı ret butonuna bastığında doğrulama
     * diyaloğu gösterir.
     */
    private fun showRejectConfirmationDialog() {
        val challenge =
            authenticatorViewModel
                .uiState
                .value
                .activeChallenge
                ?: return


        val userName =
            challenge.resolveUserDisplayName()


        MaterialAlertDialogBuilder(
            this,
        )
            .setTitle(
                "Giriş isteğini reddet",
            )
            .setMessage(
                "$userName kullanıcısı için gelen giriş isteği reddedilsin mi?",
            )
            .setNegativeButton(
                "Vazgeç",
                null,
            )
            .setPositiveButton(
                "Reddet",
            ) {
                    _,
                    _ ->

                requestLocationAndSubmitDecision(
                    approve =
                    false,
                )
            }
            .show()
    }


    /*
     * =====================================================
     * CHALLENGE KAPATMA DİYALOĞU
     * =====================================================
     */


    /**
     * Challenge kartındaki kapatma butonuna basıldığında
     * challenge yalnızca ekrandan kaldırılır.
     *
     * Bu işlem sunucuya onay veya ret göndermez.
     */
    private fun showDismissChallengeDialog() {
        val challenge =
            authenticatorViewModel
                .uiState
                .value
                .activeChallenge
                ?: return


        MaterialAlertDialogBuilder(
            this,
        )
            .setTitle(
                "Doğrulama isteğini kapat",
            )
            .setMessage(
                "Bu istek yalnızca ekrandan kaldırılacak. Sunucuya onay veya ret gönderilmeyecek.",
            )
            .setNegativeButton(
                "Vazgeç",
                null,
            )
            .setPositiveButton(
                "Kapat",
            ) {
                    _,
                    _ ->

                authenticatorViewModel
                    .dismissActiveChallenge()
            }
            .show()
    }


    /*
     * =====================================================
     * KONUM ALMA VE KARAR GÖNDERME
     * =====================================================
     */


    /**
     * Konum izni varsa konumu almaya çalışır; izin yoksa
     * kullanıcıdan çalışma zamanı izni ister.
     */
    private fun requestLocationAndSubmitDecision(
        approve: Boolean,
    ) {
        if (
            hasFineLocationPermission() ||
            hasCoarseLocationPermission()
        ) {
            captureLocationAndSubmitDecision(
                approve =
                approve,
            )

            return
        }


        pendingApproveDecision =
            approve


        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission
                    .ACCESS_FINE_LOCATION,

                Manifest.permission
                    .ACCESS_COARSE_LOCATION,
            ),
        )
    }


    /**
     * Önce son bilinen konumu kontrol eder. Uygun konum
     * yoksa GPS veya ağ sağlayıcısından tek seferlik yeni
     * konum ister.
     */
    private fun captureLocationAndSubmitDecision(
        approve: Boolean,
    ) {
        val locationManager =
            getSystemService<LocationManager>()


        if (locationManager == null) {
            submitDecision(
                approve =
                approve,

                location =
                ChallengeLocationData(
                    permissionStatus =
                    LocationPermissionStatus.UNAVAILABLE,
                ),
            )

            return
        }


        val permissionStatus =
            if (hasFineLocationPermission()) {
                LocationPermissionStatus.GRANTED_PRECISE
            } else {
                LocationPermissionStatus.GRANTED_APPROXIMATE
            }


        val enabledProviders =
            listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER,
            ).filter {
                    provider ->

                try {
                    locationManager.isProviderEnabled(
                        provider,
                    )
                } catch (_: Exception) {
                    false
                }
            }


        if (enabledProviders.isEmpty()) {
            submitDecision(
                approve =
                approve,

                location =
                ChallengeLocationData(
                    permissionStatus =
                    LocationPermissionStatus.UNAVAILABLE,
                ),
            )


            Snackbar.make(
                binding.root,
                "Telefonun konum özelliği kapalı. Karar konumsuz gönderildi.",
                Snackbar.LENGTH_LONG,
            ).show()

            return
        }


        val lastKnownLocation =
            findBestLastKnownLocation(
                locationManager =
                locationManager,

                providers =
                enabledProviders,
            )


        if (
            lastKnownLocation != null &&
            isLocationRecent(
                location =
                lastKnownLocation,
            )
        ) {
            submitDecision(
                approve =
                approve,

                location =
                createChallengeLocationData(
                    location =
                    lastKnownLocation,

                    permissionStatus =
                    permissionStatus,
                ),
            )

            return
        }


        val provider =
            when {
                hasFineLocationPermission() &&
                        enabledProviders.contains(
                            LocationManager.GPS_PROVIDER,
                        ) -> {
                    LocationManager.GPS_PROVIDER
                }

                enabledProviders.contains(
                    LocationManager.NETWORK_PROVIDER,
                ) -> {
                    LocationManager.NETWORK_PROVIDER
                }

                else -> {
                    enabledProviders.first()
                }
            }


        requestSingleLocationUpdate(
            locationManager =
            locationManager,

            provider =
            provider,

            approve =
            approve,

            permissionStatus =
            permissionStatus,

            fallbackLocation =
            lastKnownLocation,
        )
    }


    /**
     * En fazla yedi saniye güncel konum bekler.
     */
    private fun requestSingleLocationUpdate(
        locationManager: LocationManager,
        provider: String,
        approve: Boolean,
        permissionStatus: LocationPermissionStatus,
        fallbackLocation: Location?,
    ) {
        cancelActiveLocationRequest()


        val listener =
            object : LocationListener {
                override fun onLocationChanged(
                    location: Location,
                ) {
                    cancelActiveLocationRequest()


                    submitDecision(
                        approve =
                        approve,

                        location =
                        createChallengeLocationData(
                            location =
                            location,

                            permissionStatus =
                            permissionStatus,
                        ),
                    )
                }
            }


        activeLocationListener =
            listener


        val timeout =
            Runnable {
                cancelActiveLocationRequest()


                val locationData =
                    if (fallbackLocation != null) {
                        createChallengeLocationData(
                            location =
                            fallbackLocation,

                            permissionStatus =
                            permissionStatus,
                        )
                    } else {
                        ChallengeLocationData(
                            permissionStatus =
                            permissionStatus,
                        )
                    }


                submitDecision(
                    approve =
                    approve,

                    location =
                    locationData,
                )


                Snackbar.make(
                    binding.root,
                    if (fallbackLocation != null) {
                        "Güncel konum alınamadı. Son bilinen konum gönderildi."
                    } else {
                        "Konum alınamadı. Karar konumsuz gönderildi."
                    },
                    Snackbar.LENGTH_LONG,
                ).show()
            }


        activeLocationTimeout =
            timeout


        try {
            locationManager.requestSingleUpdate(
                provider,
                listener,
                Looper.getMainLooper(),
            )


            locationHandler.postDelayed(
                timeout,
                7_000L,
            )
        } catch (_: SecurityException) {
            cancelActiveLocationRequest()


            submitDecision(
                approve =
                approve,

                location =
                ChallengeLocationData(
                    permissionStatus =
                    LocationPermissionStatus.DENIED,
                ),
            )
        } catch (_: Exception) {
            cancelActiveLocationRequest()


            submitDecision(
                approve =
                approve,

                location =
                ChallengeLocationData(
                    permissionStatus =
                    LocationPermissionStatus.UNAVAILABLE,
                ),
            )
        }
    }


    /**
     * Konumla birlikte kararı ViewModel'e aktarır.
     */
    private fun submitDecision(
        approve: Boolean,
        location: ChallengeLocationData,
    ) {
        if (approve) {
            authenticatorViewModel
                .approveActiveChallenge(
                    location =
                    location,
                )
        } else {
            authenticatorViewModel
                .rejectActiveChallenge(
                    location =
                    location,
                )
        }
    }


    /**
     * Android Location nesnesini request modeline
     * dönüştürür.
     */
    private fun createChallengeLocationData(
        location: Location,
        permissionStatus: LocationPermissionStatus,
    ): ChallengeLocationData {
        return ChallengeLocationData(
            latitude =
            location.latitude,

            longitude =
            location.longitude,

            accuracyMeters =
            if (location.hasAccuracy()) {
                location.accuracy.toDouble()
            } else {
                null
            },

            permissionStatus =
            permissionStatus,

            capturedAt =
            formatUtcIsoDateTime(
                timeMilliseconds =
                if (location.time > 0L) {
                    location.time
                } else {
                    System.currentTimeMillis()
                },
            ),
        )
    }


    /**
     * Sağlayıcılardaki en yeni son bilinen konumu seçer.
     */
    private fun findBestLastKnownLocation(
        locationManager: LocationManager,
        providers: List<String>,
    ): Location? {
        return try {
            providers
                .mapNotNull {
                        provider ->

                    try {
                        locationManager
                            .getLastKnownLocation(
                                provider,
                            )
                    } catch (_: SecurityException) {
                        null
                    }
                }
                .maxByOrNull {
                    it.time
                }
        } catch (_: Exception) {
            null
        }
    }


    /**
     * Konum beş dakikadan yeni ise güncel kabul edilir.
     */
    private fun isLocationRecent(
        location: Location,
    ): Boolean {
        val age =
            System.currentTimeMillis() -
                    location.time


        return (
                location.time > 0L &&
                        age in 0L..300_000L
                )
    }


    private fun hasFineLocationPermission():
            Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun hasCoarseLocationPermission():
            Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * UTC ISO-8601 tarih metni üretir.
     */
    private fun formatUtcIsoDateTime(
        timeMilliseconds: Long,
    ): String {
        return SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US,
        ).apply {
            timeZone =
                TimeZone.getTimeZone(
                    "UTC",
                )
        }.format(
            Date(
                timeMilliseconds,
            ),
        )
    }


    /**
     * Açık konum dinleyicisini ve timeout görevini
     * temizler.
     */
    private fun cancelActiveLocationRequest() {
        activeLocationTimeout
            ?.let {
                    timeout ->

                locationHandler.removeCallbacks(
                    timeout,
                )
            }


        activeLocationTimeout =
            null


        val listener =
            activeLocationListener


        activeLocationListener =
            null


        if (listener != null) {
            try {
                getSystemService<LocationManager>()
                    ?.removeUpdates(
                        listener,
                    )
            } catch (_: Exception) {
                // Activity kapanırken hata göstermeyiz.
            }
        }
    }


    /*
     * =====================================================
     * KARAR TAMAMLANDI
     * =====================================================
     */


    /**
     * Challenge kararı başarıyla işlendiğinde Snackbar
     * gösterir ve olayı tüketir.
     */
    private fun handleDecisionCompleted(
        uiState: AuthenticatorUiState,
    ) {
        authenticatorViewModel
            .consumeDecisionCompletedEvent()


        val message =
            uiState.infoMessage
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: if (uiState.wasApproved) {
                    "Giriş isteği başarıyla onaylandı."
                } else {
                    "Giriş isteği reddedildi."
                }


        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG,
        ).show()
    }


    /*
     * =====================================================
     * GERİ TUŞU
     * =====================================================
     */


    /**
     * Aktif challenge veya devam eden karar işlemi varken
     * geri tuşu davranışını kontrollü hâle getirir.
     */
    private fun setupBackPressHandling() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(
                true,
            ) {
                override fun handleOnBackPressed() {
                    val currentState =
                        authenticatorViewModel
                            .uiState
                            .value


                    if (
                        currentState.isDecisionSubmitting
                    ) {
                        Snackbar.make(
                            binding.root,
                            "Karar gönderilirken ekrandan çıkılamaz.",
                            Snackbar.LENGTH_SHORT,
                        ).show()

                        return
                    }


                    if (
                        currentState.activeChallenge != null
                    ) {
                        MaterialAlertDialogBuilder(
                            this@AuthenticatorActivity,
                        )
                            .setTitle(
                                "Authenticator ekranından çık",
                            )
                            .setMessage(
                                "Bekleyen bir giriş doğrulama isteği var. Yine de çıkmak istiyor musunuz?",
                            )
                            .setNegativeButton(
                                "Kal",
                                null,
                            )
                            .setPositiveButton(
                                "Çık",
                            ) {
                                    _,
                                    _ ->

                                closeActivity()
                            }
                            .show()

                        return
                    }


                    closeActivity()
                }
            },
        )
    }


    /**
     * WebSocket bağlantısını kapatıp Activity'yi
     * sonlandırır.
     */
    private fun closeActivity() {
        cancelActiveLocationRequest()

        authenticatorViewModel.disconnect()

        finish()
    }


    override fun onDestroy() {
        cancelActiveLocationRequest()

        super.onDestroy()
    }
}