package com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRecipientUser
import com.alperensarac.projectmanagementkotlin.domain.repository.MailboxRecipientRepository
import javax.inject.Inject

class SearchMailboxRecipientsUseCase @Inject constructor(
    private val repository:
    MailboxRecipientRepository
) {

    suspend operator fun invoke(
        search: String?
    ): AppResult<List<MailboxRecipientUser>> {

        /*
         * Backend UserListQuery validator'ını gereksiz yere
         * zorlamamak için UI search'i sınırlıyoruz.
         */
        val normalizedSearch =
            search
                ?.trim()
                ?.take(250)
                ?.takeIf {
                    it.isNotBlank()
                }

        return repository.searchRecipients(
            normalizedSearch
        )
    }
}