package com.alperensarac.projectmanagementkotlin.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.auth.session.SessionEventBus
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.usecase.auth.GetCurrentUserUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Profil ekranının kullanıcı bilgilerini ve logout işlemini yönetir.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(
        ProfileUiState()
    )

    val uiState: StateFlow<ProfileUiState> =
        mutableUiState.asStateFlow()

    /**
     * Snackbar gibi tek seferlik UI olayları için Channel kullanılır.
     */
    private val eventChannel =
        Channel<ProfileUiEvent>(capacity = Channel.BUFFERED)

    val events = eventChannel.receiveAsFlow()

    init {
        loadProfile()
    }

    /**
     * GET /api/Auth/me üzerinden kullanıcı bilgilerini yükler.
     */
    fun loadProfile() {
        val currentState = mutableUiState.value

        /*
         * Aynı anda birden fazla profil isteği gönderilmesini engeller.
         */
        if (currentState.isLoading && currentState.user != null) {
            return
        }

        viewModelScope.launch {
            mutableUiState.value =
                mutableUiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

            when (
                val result = getCurrentUserUseCase()
            ) {
                is AppResult.Success -> {
                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isLoading = false,
                            user = result.data,
                            errorMessage = null
                        )
                }

                is AppResult.Error -> {
                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isLoading = false,
                            errorMessage = result.error.toUserMessage()
                        )
                }
            }
        }
    }

    /**
     * Kullanıcının mevcut cihazdaki oturumunu kapatır.
     */
    fun logout() {
        val currentState = mutableUiState.value

        if (currentState.isLoggingOut) {
            return
        }

        viewModelScope.launch {
            mutableUiState.value =
                currentState.copy(
                    isLoggingOut = true,
                    errorMessage = null
                )

            when (
                val result = logoutUseCase()
            ) {
                is AppResult.Success -> {
                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isLoggingOut = false
                        )

                    /*
                     * Profil Fragment nested home navigation içerisindedir.
                     *
                     * Buradan ana Login ekranına doğrudan yönlenmek yerine
                     * MainActivity tarafından dinlenen global oturum olayı
                     * yayınlanır.
                     */
                    sessionEventBus.notifyUserLoggedOut()
                }

                is AppResult.Error -> {
                    /*
                     * AuthRepository logout sırasında network hatası olsa bile
                     * yerel tokenları temizlemektedir.
                     *
                     * Bu nedenle kullanıcıyı cihaz üzerinde oturumdan çıkmış
                     * kabul ederek Login ekranına yönlendiriyoruz.
                     */
                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isLoggingOut = false
                        )

                    eventChannel.send(
                        ProfileUiEvent.ShowMessage(
                            message = result.error.toUserMessage()
                        )
                    )

                    sessionEventBus.notifyUserLoggedOut()
                }
            }
        }
    }
}