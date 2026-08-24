package com.alperensarac.projectmanagementkotlin.data.remote.dto.users

import kotlinx.serialization.Serializable

/**
 * PATCH /api/Users/{id}/status
 */
@Serializable
data class UpdateUserStatusRequestDto(
    val isActive: Boolean
)