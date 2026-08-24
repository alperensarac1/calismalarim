package com.alperensarac.projectmanagementkotlin.domain.model.mailbox

/**
 * Inbox / Sent listelerinde kullanılan domain model.
 */
data class MailboxMessage(
    val id: Int,

    val subject: String,

    val bodyPreview: String,

    val sender: MailboxUser,

    val recipients: List<MailboxUser>,

    val sentAtUtc: String,

    val isRead: Boolean?,

    val readAtUtc: String?,

    val hasAttachment: Boolean,

    val attachmentCount: Int
)