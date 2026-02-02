package com.example.kargopaylasimjetpack.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kargopaylasimjetpack.model.RegisterReq
import com.example.kargopaylasimjetpack.repository.Repo
import com.example.kargopaylasimjetpack.storage.TokenStore
import com.example.kargopaylasimjetpack.util.UiState

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthVM(
    private val repo: Repo,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val loginState: StateFlow<UiState<Boolean>> = _loginState

    private val _registerState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val registerState: StateFlow<UiState<Boolean>> = _registerState

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val r = repo.login(phone, password)
                if (!r.ok || r.data == null) throw Exception(r.error ?: "Invalid credentials")
                tokenStore.setToken(r.data.token)
                _loginState.value = UiState.Success(true)
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "Login error")
            }
        }
    }

    fun register(req: RegisterReq) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            try {
                val r = repo.register(req)
                if (!r.ok || r.data == null) throw Exception(r.error ?: "Register failed")
                _registerState.value = UiState.Success(true)
            } catch (e: Exception) {
                _registerState.value = UiState.Error(e.message ?: "Register error")
            }
        }
    }
}
