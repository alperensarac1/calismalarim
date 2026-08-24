package com.alperensarac.projectmanagementkotlin.feature.profile

import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser

/**
 * Profil ekranının kalıcı UI durumudur.
 */
data class ProfileUiState(
    /**
     * Kullanıcı bilgileri yüklenirken gösterilir.
     */
    val isLoading: Boolean = true,

    /**
     * Logout işlemi devam ederken kullanılır.
     */
    val isLoggingOut: Boolean = false,

    /**
     * Backend'den getirilen oturum kullanıcısıdır.
     */
    val user: AuthUser? = null,

    /**
     * Profil bilgileri alınırken oluşan genel hata mesajıdır.
     */
    val errorMessage: String? = null
) {

    /**
     * Kullanıcı bilgileri başarıyla yüklendiyse true döner.
     */
    val hasUser: Boolean
        get() = user != null
}