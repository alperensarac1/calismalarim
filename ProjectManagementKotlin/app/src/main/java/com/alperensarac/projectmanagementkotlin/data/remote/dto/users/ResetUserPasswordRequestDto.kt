package com.alperensarac.projectmanagementkotlin.data.remote.dto.users

import kotlinx.serialization.Serializable

/**
 * PATCH /api/Users/{id}/reset-password
 */
@Serializable
data class ResetUserPasswordRequestDto(
    val newPassword: String
)