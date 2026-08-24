package com.alperensarac.projectmanagementkotlin.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Login ekranının state ve business akışını yönetir.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginValidator: LoginValidator
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(
        LoginUiState()
    )

    val uiState: StateFlow<LoginUiState> =
        mutableUiState.asStateFlow()

    /**
     * Navigation ve Snackbar gibi tek seferlik olaylar için Channel kullanılır.
     */
    private val eventChannel =
        Channel<LoginUiEvent>(capacity = Channel.BUFFERED)

    val events = eventChannel.receiveAsFlow()

    fun onEmailChanged(email: String) {
        mutableUiState.value = mutableUiState.value.copy(
            email = email,
            emailError = null,
            generalError = null
        )
    }

    fun onPasswordChanged(password: String) {
        mutableUiState.value = mutableUiState.value.copy(
            password = password,
            passwordError = null,
            generalError = null
        )
    }

    fun login() {
        val currentState = mutableUiState.value

        /*
         * Kullanıcı butona art arda basarsa birden fazla login isteği
         * gönderilmesini engeller.
         */
        if (currentState.isLoading) {
            return
        }

        val validationResult = loginValidator.validate(
            email = currentState.email,
            password = currentState.password
        )

        if (!validationResult.isValid) {
            mutableUiState.value = currentState.copy(
                emailError = validationResult.emailError,
                passwordError = validationResult.passwordError,
                generalError = null
            )

            return
        }

        viewModelScope.launch {
            mutableUiState.value = currentState.copy(
                isLoading = true,
                emailError = null,
                passwordError = null,
                generalError = null
            )

            when (
                val result = loginUseCase(
                    email = currentState.email.trim(),
                    password = currentState.password
                )
            ) {
                is AppResult.Success -> {
                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isLoading = false,
                            generalError = null
                        )

                    eventChannel.send(
                        LoginUiEvent.NavigateToHome
                    )
                }

                is AppResult.Error -> {
                    handleLoginError(result.error)
                }
            }
        }
    }

    /**
     * Backend validation hatalarını mümkün olduğunda ilgili form alanlarına
     * dağıtır. Diğer hataları genel hata mesajı olarak gösterir.
     */
    private fun handleLoginError(
        error: NetworkError
    ) {
        if (error is NetworkError.Validation) {
            val emailError = findFieldError(
                errors = error.fieldErrors,
                fieldName = "email"
            )

            val passwordError = findFieldError(
                errors = error.fieldErrors,
                fieldName = "password"
            )

            mutableUiState.value =
                mutableUiState.value.copy(
                    isLoading = false,
                    emailError = emailError,
                    passwordError = passwordError,
                    generalError = if (
                        emailError == null &&
                        passwordError == null
                    ) {
                        error.message
                    } else {
                        null
                    }
                )

            return
        }

        mutableUiState.value =
            mutableUiState.value.copy(
                isLoading = false,
                generalError = error.toUserMessage()
            )
    }

    /**
     * Backend validation dictionary anahtarlarını büyük/küçük harf duyarsız
     * olarak arar.
     */
    private fun findFieldError(
        errors: Map<String, List<String>>,
        fieldName: String
    ): String? {
        return errors.entries
            .firstOrNull { (key, _) ->
                key.equals(
                    other = fieldName,
                    ignoreCase = true
                )
            }
            ?.value
            ?.firstOrNull()
    }
}