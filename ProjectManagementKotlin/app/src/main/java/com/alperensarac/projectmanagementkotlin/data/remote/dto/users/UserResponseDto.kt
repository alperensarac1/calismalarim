package com.alperensarac.projectmanagementkotlin.data.remote.dto.users

import kotlinx.serialization.Serializable

/**
 * /api/Users endpointlerinden dönen kullanıcı DTO'sudur.
 */
@Serializable
data class UserResponseDto(

    val id: Int,

    val firstName: String,

    val lastName: String,

    val fullName: String,

    val email: String,

    val role: String,

    val department: String? = null,

    val isActive: Boolean,

    val createdAt: String
)