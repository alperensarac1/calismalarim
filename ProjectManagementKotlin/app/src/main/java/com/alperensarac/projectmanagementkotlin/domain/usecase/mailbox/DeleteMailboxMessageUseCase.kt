package com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.repository.MailboxRepository
import javax.inject.Inject

class DeleteMailboxMessageUseCase @Inject constructor(
    private val repository: MailboxRepository
) {

    suspend operator fun invoke(
        messageId: Int
    ): AppResult<Unit> {

        require(messageId > 0) {
            "Message id sıfırdan büyük olmalıdır."
        }

        return repository.deleteMessage(
            messageId
        )
    }
}