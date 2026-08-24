package com.alperensarac.projectmanagementkotlin.data.mapper.mailbox

import com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox.MailboxAttachmentResponseDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox.MailboxMessageDetailDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox.MailboxMessageListItemDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox.MailboxUserDto
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxAttachment
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessage
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessageDetail
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxUser

fun MailboxUserDto.toDomain(): MailboxUser {

    return MailboxUser(
        id = id,
        firstName = firstName,
        lastName = lastName,
        fullName = fullName,
        email = email
    )
}

fun MailboxAttachmentResponseDto.toDomain(): MailboxAttachment {

    return MailboxAttachment(
        id = id,
        messageId = messageId,
        originalFileName = originalFileName,
        contentType = contentType,
        extension = extension,
        fileSize = fileSize,
        uploadedAtUtc = uploadedAtUtc,
        expiresAtUtc = expiresAtUtc,
        fileDeletedAtUtc = fileDeletedAtUtc,
        isFileDeleted = isFileDeleted,
        isAvailable = isAvailable
    )
}

fun MailboxMessageListItemDto.toDomain(): MailboxMessage {

    return MailboxMessage(
        id = id,

        subject = subject,

        bodyPreview = bodyPreview,

        sender = sender.toDomain(),

        recipients =
        recipients.map {
            it.toDomain()
        },

        sentAtUtc = sentAtUtc,

        isRead = isRead,

        readAtUtc = readAtUtc,

        hasAttachment = hasAttachment,

        attachmentCount = attachmentCount
    )
}

fun MailboxMessageDetailDto.toDomain(): MailboxMessageDetail {

    return MailboxMessageDetail(
        id = id,

        subject = subject,

        body = body,

        sender = sender.toDomain(),

        recipients =
        recipients.map {
            it.toDomain()
        },

        sentAtUtc = sentAtUtc,

        isRead = isRead,

        readAtUtc = readAtUtc,

        attachments =
        attachments.map {
            it.toDomain()
        }
    )
}