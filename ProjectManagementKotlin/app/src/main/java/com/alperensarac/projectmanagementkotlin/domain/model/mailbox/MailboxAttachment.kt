package com.alperensarac.projectmanagementkotlin.domain.model.mailbox

data class MailboxAttachment(
    val id: Int,

    val messageId: Int,

    val originalFileName: String,

    val contentType: String,

    val extension: String,

    val fileSize: Long,

    val uploadedAtUtc: String,

    val expiresAtUtc: String,

    val fileDeletedAtUtc: String?,

    val isFileDeleted: Boolean,

    val isAvailable: Boolean
)