package com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox

import kotlinx.serialization.Serializable

/**
 * Backend:
 *
 * MailboxUserDto
 *
 * Mailbox içerisindeki gönderen ve alıcı kullanıcıların
 * temel bilgilerini taşır.
 */
@Serializable
data class MailboxUserDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String
)