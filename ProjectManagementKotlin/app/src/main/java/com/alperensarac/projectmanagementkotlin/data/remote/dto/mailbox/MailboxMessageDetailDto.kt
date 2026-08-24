package com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox

import kotlinx.serialization.Serializable

/**
 * Mesaj detay endpointinin network DTO'sudur.
 */
@Serializable
data class MailboxMessageDetailDto(
    val id: Int,

    val subject: String,

    val body: String,

    val sender: MailboxUserDto,

    val recipients: List<MailboxUserDto> = emptyList(),

    val sentAtUtc: String,

    val isRead: Boolean? = null,

    val readAtUtc: String? = null,

    val attachments: List<MailboxAttachmentResponseDto> = emptyList()
)