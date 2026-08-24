package com.alperensarac.projectmanagementkotlin.feature.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.usecase.auth.RefreshSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Splash ekranının oturum kontrolü ve yönlendirme akışını yönetir.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val refreshSessionUseCase: RefreshSessionUseCase
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(
        SplashUiState()
    )

    val uiState: StateFlow<SplashUiState> =
        mutableUiState.asStateFlow()

    /**
     * Navigation olayları için Channel kullanılır.
     */
    private val eventChannel =
        Channel<SplashUiEvent>(capacity = Channel.BUFFERED)

    val events = eventChannel.receiveAsFlow()

    /**
     * ViewModel ilk oluşturulduğunda oturum kontrolü otomatik başlatılır.
     */
    init {
        checkSession()
    }

    /**
     * Şifrelenmiş refresh token üzerinden oturumu yenilemeyi dener.
     */
    private fun checkSession() {
        viewModelScope.launch {
            mutableUiState.value = SplashUiState(
                isCheckingSession = true,
                statusMessage = "Oturum kontrol ediliyor…"
            )

            /*
             * Splash ekranının çok kısa süre görünerek titreşim hissi
             * oluşturmasını engellemek için küçük bir minimum bekleme
             * kullanıyoruz.
             *
             * Bu bekleme gerçek network işlemine ek olarak uygulanır.
             */
            delay(MINIMUM_SPLASH_DURATION_MILLIS)

            when (val result = refreshSessionUseCase()) {
                is AppResult.Success -> {
                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isCheckingSession = false,
                            statusMessage = "Oturum açılıyor…"
                        )

                    eventChannel.send(
                        SplashUiEvent.NavigateToHome
                    )
                }

                is AppResult.Error -> {
                    handleRefreshError(result.error)
                }
            }
        }
    }

    /**
     * Oturum yenileme hatasını değerlendirir.
     */
    private suspend fun handleRefreshError(
        error: NetworkError
    ) {
        mutableUiState.value =
            mutableUiState.value.copy(
                isCheckingSession = false,
                statusMessage = "Giriş ekranına yönlendiriliyor…"
            )

        when (error) {
            /*
             * Kayıtlı refresh token bulunmaması veya refresh tokenın geçersiz
             * olması normal bir oturumsuz başlangıç durumudur.
             *
             * Bu durumda kullanıcıya ayrıca hata göstermiyoruz.
             */
            is NetworkError.Unauthorized -> {
                eventChannel.send(
                    SplashUiEvent.NavigateToLogin()
                )
            }

            /*
             * Ağ veya sunucu problemi varsa kullanıcı Login ekranına
             * yönlendirilir ancak açıklayıcı bir mesaj gösterilir.
             *
             * AuthRepository yalnızca 401 durumunda tokenları temizlediği için
             * geçici bağlantı probleminde refresh token korunur.
             */
            else -> {
                eventChannel.send(
                    SplashUiEvent.NavigateToLogin(
                        message = error.toUserMessage()
                    )
                )
            }
        }
    }

    private companion object {
        const val MINIMUM_SPLASH_DURATION_MILLIS = 500L
    }
}