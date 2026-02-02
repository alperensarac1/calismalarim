package com.example.kargopaylasimkotlin.viewmodel


import androidx.lifecycle.*
import com.example.kargopaylasimkotlin.dto.AddressCreateReq
import com.example.kargopaylasimkotlin.dto.RegisterReq
import com.example.kargopaylasimkotlin.model.UiState
import com.example.kargopaylasimkotlin.repo.CargoRepository
import com.example.kargopaylasimkotlin.service.TokenStore

import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: CargoRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _loginState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val loginState: LiveData<UiState<Unit>> = _loginState

    private val _registerState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val registerState: LiveData<UiState<Unit>> = _registerState

    fun login(phone: String, password: String) {
        _loginState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.login(phone, password)
                if (resp.ok && resp.data != null) {
                    tokenStore.saveToken(resp.data.token)
                    _loginState.value = UiState.Success(Unit)
                } else _loginState.value = UiState.Error(resp.error ?: "Login failed")
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    /**
     * Register endpoint artık USER + DEFAULT ADDRESS oluşturuyor varsayımıyla
     */
    fun register(req: RegisterReq) {
        _registerState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.register(req)
                if (resp.ok) {
                    _registerState.value = UiState.Success(Unit)
                } else {
                    _registerState.value = UiState.Error(resp.error ?: "Register failed")
                }
            } catch (e: Exception) {
                _registerState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    /**
     * Eski kod yapın bozulmasın diye tutuyoruz.
     * Artık addressCreate YAPMIYOR.
     * Sadece register + (opsiyonel) auto-login yapıyor.
     */
    fun registerAndSetup(registerReq: RegisterReq, addressReq: AddressCreateReq) {
        _registerState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val reg = repo.register(registerReq)
                if (!reg.ok) {
                    _registerState.value = UiState.Error(reg.error ?: "Register failed")
                    return@launch
                }

                // Auto-login istersen kalsın (istersen bunu da kaldırırız)
                val login = repo.login(registerReq.phone, registerReq.password)
                if (!login.ok || login.data == null) {
                    _registerState.value = UiState.Error(login.error ?: "Login after register failed")
                    return@launch
                }
                tokenStore.saveToken(login.data.token)

                // ✅ Address create kaldırıldı (register endpoint zaten default adresi ekliyor)
                _registerState.value = UiState.Success(Unit)

            } catch (e: Exception) {
                _registerState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }
}
