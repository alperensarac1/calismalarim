package com.alperensarac.projectmanagementkotlin.domain.model.mailbox

/**
 * Mesaj detay ekranının domain modeli.
 */
data class MailboxMessageDetail(
    val id: Int,

    val subject: String,

    val body: String,

    val sender: MailboxUser,

    val recipients: List<MailboxUser>,

    val sentAtUtc: String,

    val isRead: Boolean?,

    val readAtUtc: String?,

    val attachments: List<MailboxAttachment>
)