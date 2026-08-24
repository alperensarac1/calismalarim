package com.alperensarac.projectmanagementkotlin.data.remote.dto.auth

import kotlinx.serialization.Serializable

/**
 * POST /api/Auth/refresh endpoint'ine gönderilen request modelidir.
 *
 * Backend yalnızca refresh token beklemektedir.
 */
@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String
)