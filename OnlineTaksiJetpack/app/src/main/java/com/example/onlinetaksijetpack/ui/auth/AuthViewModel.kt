package com.example.onlinetaksijetpack.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinetaksijetpack.data.local.SessionManager
import com.example.onlinetaksijetpack.data.remote.model.LoginRequest
import com.example.onlinetaksijetpack.data.remote.model.RegisterRequest
import com.example.onlinetaksijetpack.data.repository.AuthRepository
import com.example.onlinetaksijetpack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val successRole: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            when (val result = authRepository.login(LoginRequest(phone, password))) {
                is Resource.Success -> {
                    val data = result.data
                    sessionManager.saveAuth(
                        token = data.access_token,
                        userId = data.user_id,
                        fullName = data.full_name,
                        role = data.role
                    )
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        message = "Giriş başarılı",
                        successRole = data.role
                    )
                }

                is Resource.Error -> {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        message = result.message
                    )
                }

                else -> {}
            }
        }
    }

    fun register(
        fullName: String,
        phone: String,
        email: String?,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            when (
                val result = authRepository.register(
                    RegisterRequest(
                        full_name = fullName,
                        phone = phone,
                        email = email,
                        password = password,
                        role = "customer"
                    )
                )
            ) {
                is Resource.Success -> {
                    val data = result.data
                    sessionManager.saveAuth(
                        token = data.access_token,
                        userId = data.user_id,
                        fullName = data.full_name,
                        role = data.role
                    )
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        message = "Kayıt başarılı",
                        successRole = data.role
                    )
                }

                is Resource.Error -> {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        message = result.message
                    )
                }

                else -> {}
            }
        }
    }
}