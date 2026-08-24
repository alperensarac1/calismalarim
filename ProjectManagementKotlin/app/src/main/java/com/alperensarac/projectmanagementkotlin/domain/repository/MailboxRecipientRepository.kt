package com.alperensarac.projectmanagementkotlin.domain.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRecipientUser

interface MailboxRecipientRepository {

    suspend fun searchRecipients(
        search: String?
    ): AppResult<List<MailboxRecipientUser>>
}