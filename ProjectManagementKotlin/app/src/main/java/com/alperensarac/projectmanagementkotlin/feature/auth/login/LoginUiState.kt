package com.alperensarac.projectmanagementkotlin.feature.auth.login

/**
 * Login ekranının kalıcı UI durumudur.
 *
 * TextInputLayout hata mesajları, loading durumu ve genel API mesajı
 * bu model üzerinden yönetilir.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false
)