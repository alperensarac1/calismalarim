package com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox

import kotlinx.serialization.Serializable

/**
 * Inbox / Sent listelerinde gösterilecek tek mesaj satırı.
 */
@Serializable
data class MailboxMessageListItemDto(
    val id: Int,

    val subject: String,

    val bodyPreview: String,

    val sender: MailboxUserDto,

    val recipients: List<MailboxUserDto> = emptyList(),

    val sentAtUtc: String,

    /**
     * Sent kutusunda null olabilir.
     */
    val isRead: Boolean? = null,

    val readAtUtc: String? = null,

    val hasAttachment: Boolean,

    val attachmentCount: Int
)