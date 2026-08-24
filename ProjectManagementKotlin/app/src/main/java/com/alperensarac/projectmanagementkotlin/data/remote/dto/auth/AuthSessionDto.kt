package com.alperensarac.projectmanagementkotlin.data.remote.dto.auth

import kotlinx.serialization.Serializable

/**
 * Login ve refresh işlemlerinin response içerisindeki data modelidir.
 *
 * Refresh endpoint'i yeni access token ile birlikte yeni refresh token
 * döndürmektedir. Bu nedenle başarılı her refresh işleminde eski refresh
 * token tamamen değiştirilmelidir.
 */
@Serializable
data class AuthSessionDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresAtUtc: String,
    val user: AuthUserDto
)