package com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox

import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFilter
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFolder
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessage
import com.alperensarac.projectmanagementkotlin.domain.repository.MailboxRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetMailboxMessagesUseCase @Inject constructor(
    private val repository: MailboxRepository
) {

    operator fun invoke(
        folder: MailboxFolder,
        filter: MailboxFilter
    ): Flow<PagingData<MailboxMessage>> {

        return repository.getMessages(
            folder = folder,
            filter = filter
        )
    }
}