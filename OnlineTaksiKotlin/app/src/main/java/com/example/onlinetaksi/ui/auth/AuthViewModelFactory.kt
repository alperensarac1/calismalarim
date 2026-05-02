package com.example.onlinetaksi.ui.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.onlinetaksi.data.local.SessionManager
import com.example.onlinetaksi.data.repository.AuthRepository

class AuthViewModelFactory(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repository, sessionManager) as T
    }
}