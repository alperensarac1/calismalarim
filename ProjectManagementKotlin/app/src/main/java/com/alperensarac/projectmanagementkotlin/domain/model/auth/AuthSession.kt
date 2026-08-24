package com.alperensarac.projectmanagementkotlin.domain.model.auth

/**
 * Başarılı login veya refresh işlemi sonucunda oluşan domain oturum modelidir.
 */
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresAtUtc: String,
    val user: AuthUser
)