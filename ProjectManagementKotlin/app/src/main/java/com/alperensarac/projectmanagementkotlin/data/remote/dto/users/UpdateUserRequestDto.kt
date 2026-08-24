package com.alperensarac.projectmanagementkotlin.data.remote.dto.users

import kotlinx.serialization.Serializable

/**
 * PUT /api/Users/{id}
 *
 * Backend UpdateUserRequestDto karşılığıdır.
 *
 * Dikkat:
 * IsActive burada YOKTUR.
 * Aktif/pasif işlemi ayrı status endpoint'i üzerinden yapılır.
 */
@Serializable
data class UpdateUserRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val department: String? = null
)