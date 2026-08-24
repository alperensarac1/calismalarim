package com.alperensarac.projectmanagementkotlin.data.mapper.mailbox

import com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox.MailboxRecipientUserDto
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRecipientUser

fun MailboxRecipientUserDto.toDomain(): MailboxRecipientUser {

    return MailboxRecipientUser(
        id = id,
        firstName = firstName,
        lastName = lastName,
        fullName = fullName,
        email = email,
        isActive = isActive
    )
}