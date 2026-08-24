package com.alperensarac.projectmanagementkotlin.data.remote.dto.users

import kotlinx.serialization.Serializable

/**
 * POST /api/Users isteğinin request modelidir.
 *
 * Backend:
 * CreateUserRequestDto
 */
@Serializable
data class CreateUserRequestDto(

    val firstName: String,

    val lastName: String,

    val email: String,

    val password: String,

    /**
     * Admin
     * ProjectManager
     * TeamMember
     */
    val role: String,

    val department: String? = null,

    /**
     * Backend varsayılanı true.
     */
    val isActive: Boolean = true
)