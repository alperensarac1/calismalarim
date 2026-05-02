package com.example.onlinetaksijetpack.ui.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.onlinetaksijetpack.data.local.SessionManager
import com.example.onlinetaksijetpack.data.repository.AuthRepository

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(authRepository, sessionManager) as T
    }
}