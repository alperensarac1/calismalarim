package com.alperensarac.projectmanagementkotlin.data.remote.dto.auth

import kotlinx.serialization.Serializable

/**
 * POST /api/Auth/logout endpoint'ine gönderilen request modelidir.
 *
 * Swagger request body:
 *
 * {
 *   "refreshToken": "..."
 * }
 */
@Serializable
data class LogoutRequestDto(
    val refreshToken: String
)