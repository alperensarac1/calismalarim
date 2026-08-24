package com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessageDetail
import com.alperensarac.projectmanagementkotlin.domain.repository.MailboxRepository
import javax.inject.Inject

class GetMailboxMessageDetailUseCase @Inject constructor(
    private val repository: MailboxRepository
) {

    suspend operator fun invoke(
        messageId: Int,
        markAsRead: Boolean = true
    ): AppResult<MailboxMessageDetail> {

        require(messageId > 0) {
            "Message id sıfırdan büyük olmalıdır."
        }

        return repository.getMessageById(
            messageId = messageId,
            markAsRead = markAsRead
        )
    }
}