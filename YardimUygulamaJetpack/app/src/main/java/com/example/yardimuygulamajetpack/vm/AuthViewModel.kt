package com.example.yardimuygulamajetpack.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yardimuygulamajetpack.entity.Session
import com.example.yardimuygulamajetpack.model.RegisterBody
import com.example.yardimuygulamajetpack.repo.AuthRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepo = AuthRepo()
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val authState: StateFlow<UiState<Unit>> = _authState

    fun login(ctx: Context, phone: String, pass: String, onSuccess: (role: String) -> Unit) {
        if (phone.isBlank() || pass.isBlank()) {
            _authState.value = UiState.Error("Telefon ve şifre zorunlu")
            return
        }
        _authState.value = UiState.Loading
        viewModelScope.launch {
            val res = runCatching { repo.login(phone, pass) }.getOrNull()
            if (res?.ok == true && res.user != null) {
                Session.save(ctx, res.user.id, res.user.role)
                _authState.value = UiState.Data(Unit)
                onSuccess(res.user.role)
            } else {
                _authState.value = UiState.Error(res?.error ?: "Giriş başarısız")
            }
        }
    }

    fun register(ctx: Context, body: RegisterBody, onSuccess: (role: String) -> Unit) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            val res = runCatching { repo.register(body) }.getOrNull()
            if (res?.ok == true && res.user != null) {
                Session.save(ctx, res.user.id, res.user.role)
                _authState.value = UiState.Data(Unit)
                onSuccess(res.user.role)
            } else {
                _authState.value = UiState.Error(res?.error ?: "Kayıt başarısız")
            }
        }
    }
}