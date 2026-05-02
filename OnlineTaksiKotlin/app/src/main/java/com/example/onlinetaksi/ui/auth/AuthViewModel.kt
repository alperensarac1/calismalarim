package com.example.onlinetaksi.ui.auth

import androidx.lifecycle.*
import com.example.onlinetaksi.data.local.SessionManager
import com.example.onlinetaksi.data.remote.model.LoginRequest
import com.example.onlinetaksi.data.remote.model.RegisterRequest
import com.example.onlinetaksi.data.repository.AuthRepository
import com.example.onlinetaksi.util.Resource
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _registerState = MutableLiveData<Resource<String>>()
    val registerState: LiveData<Resource<String>> = _registerState

    private val _loginState = MutableLiveData<Resource<String>>()
    val loginState: LiveData<Resource<String>> = _loginState

    fun register(
        fullName: String,
        phone: String,
        email: String?,
        password: String
    ) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading

            val result = repository.register(
                RegisterRequest(
                    full_name = fullName,
                    phone = phone,
                    email = email,
                    password = password,
                    role = "customer"
                )
            )

            when (result) {
                is Resource.Success -> {
                    val data = result.data
                    sessionManager.saveAuth(
                        token = data.access_token,
                        userId = data.user_id,
                        fullName = data.full_name,
                        role = data.role
                    )
                    _registerState.value = Resource.Success("Kayıt başarılı")
                }
                is Resource.Error -> {
                    _registerState.value = Resource.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading

            val result = repository.login(
                LoginRequest(
                    phone = phone,
                    password = password
                )
            )

            when (result) {
                is Resource.Success -> {
                    val data = result.data
                    sessionManager.saveAuth(
                        token = data.access_token,
                        userId = data.user_id,
                        fullName = data.full_name,
                        role = data.role
                    )
                    _loginState.value = Resource.Success("Giriş başarılı")
                }
                is Resource.Error -> {
                    _loginState.value = Resource.Error(result.message)
                }
                else -> {}
            }
        }
    }
}