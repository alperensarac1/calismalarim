package com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.repository.MailboxRepository
import javax.inject.Inject

class MarkMailboxMessageAsUnreadUseCase @Inject constructor(
    private val repository: MailboxRepository
) {

    suspend operator fun invoke(
        messageId: Int
    ): AppResult<Unit> {

        require(messageId > 0)

        return repository.markAsUnread(
            messageId
        )
    }
}