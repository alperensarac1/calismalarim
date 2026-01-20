package com.example.eticaretjetpack.viewmodel

// ui/auth/AuthViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eticaretjetpack.model.UserDto
import com.example.eticaretjetpack.repo.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class AuthViewModel(
    private val repo: AuthRepositoryImpl
) : ViewModel() {

    data class AuthState(
        val inFlight: Boolean = false,
        val error: String? = null,
        val loggedIn: Boolean = false,
        val registered: Boolean = false,   // ✅ eklendi
        val me: UserDto? = null
    )

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String) = viewModelScope.launch {
        _state.update { it.copy(inFlight = true, error = null, loggedIn = false) }

        repo.login(email, password)
            .onSuccess {
                _state.update { it.copy(inFlight = false, loggedIn = true) }
            }
            .onFailure { e ->
                _state.update { it.copy(inFlight = false, error = e.message ?: "Login failed") }
            }
    }

    fun register(name: String, email: String, password: String) = viewModelScope.launch {
        _state.update { it.copy(inFlight = true, error = null, registered = false) }

        repo.register(name, email, password)
            .onSuccess {
                // ✅ register başarılı → loggedIn yapmıyoruz, sadece registered=true
                _state.update { it.copy(inFlight = false, registered = true) }
            }
            .onFailure { e ->
                _state.update { it.copy(inFlight = false, error = e.message ?: "Register failed") }
            }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    fun clearRegistered() = _state.update { it.copy(registered = false) } // ✅ eklendi
}
