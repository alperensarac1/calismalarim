package com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox

import kotlinx.serialization.Serializable

/**
 * Bir mailbox mesajındaki attachment metadata bilgisidir.
 *
 * Burada dosyanın binary içeriği yoktur.
 *
 * Binary veri ayrı download endpointinden alınır.
 */
@Serializable
data class MailboxAttachmentResponseDto(
    val id: Int,
    val messageId: Int,
    val originalFileName: String,
    val contentType: String,
    val extension: String,
    val fileSize: Long,
    val uploadedAtUtc: String,
    val expiresAtUtc: String,
    val fileDeletedAtUtc: String? = null,
    val isFileDeleted: Boolean,
    val isAvailable: Boolean
)