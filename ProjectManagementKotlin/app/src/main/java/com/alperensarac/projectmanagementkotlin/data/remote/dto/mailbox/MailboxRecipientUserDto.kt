package com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox

import kotlinx.serialization.Serializable

/**
 * GET /api/Users endpointinden mailbox recipient seçimi için
 * ihtiyaç duyduğumuz kullanıcı alanları.
 *
 * Backend UserResponseDto daha fazla alan döndürebilir.
 * kotlinx.serialization Json ignoreUnknownKeys=true olduğu sürece
 * ihtiyacımız olmayan alanları tanımlamak zorunda değiliz.
 */
@Serializable
data class MailboxRecipientUserDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,
    val isActive: Boolean
)