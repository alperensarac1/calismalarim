package com.alperensarac.projectmanagementkotlin.data.remote.dto.auth

import kotlinx.serialization.Serializable

/**
 * POST /api/Auth/login endpoint'ine gönderilen request modelidir.
 *
 * Backend request örneği:
 *
 * {
 *   "email": "admin@projectmanagement.local",
 *   "password": "12345678Aa"
 * }
 */
@Serializable
data class LoginRequestDto(
    /**
     * Kullanıcının sisteme kayıtlı e-posta adresidir.
     */
    val email: String,

    /**
     * Kullanıcının giriş şifresidir.
     *
     * Bu değer uygulamada kalıcı olarak saklanmayacaktır.
     */
    val password: String
)