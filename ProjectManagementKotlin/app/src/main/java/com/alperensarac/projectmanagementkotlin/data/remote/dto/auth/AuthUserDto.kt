package com.alperensarac.projectmanagementkotlin.data.remote.dto.auth

import kotlinx.serialization.Serializable

/**
 * Authentication endpointlerinden dönen kullanıcı DTO modelidir.
 *
 * Bu sınıf backend JSON sözleşmesini temsil eder.
 * UI katmanında doğrudan kullanılmayacaktır.
 */
@Serializable
data class AuthUserDto(
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