package com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox

import com.alperensarac.projectmanagementkotlin.data.upload.MailboxSelectedFile
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRules
import javax.inject.Inject

/**
 * SendMailboxMessageValidator kurallarının Android
 * tarafındaki ön doğrulaması.
 */
class ValidateMailboxMessageUseCase @Inject constructor(
    private val validateAttachmentsUseCase:
    ValidateMailboxAttachmentsUseCase
) {

    operator fun invoke(
        recipientUserIds: List<Int>,
        subject: String,
        body: String,
        attachments: List<MailboxSelectedFile>
    ): Result<Unit> {

        // ---------------------------------------------------------------------
        // RECIPIENT
        // ---------------------------------------------------------------------

        if (
            recipientUserIds.isEmpty()
        ) {

            return Result.failure(
                IllegalArgumentException(
                    "En az bir alıcı seçilmelidir."
                )
            )
        }

        if (
            recipientUserIds.size >
            MailboxRules.MAXIMUM_RECIPIENT_COUNT
        ) {

            return Result.failure(
                IllegalArgumentException(
                    "Bir mesaj en fazla 50 kullanıcıya gönderilebilir."
                )
            )
        }

        if (
            recipientUserIds
                .distinct()
                .size !=
            recipientUserIds.size
        ) {

            return Result.failure(
                IllegalArgumentException(
                    "Aynı kullanıcı alıcı listesine birden fazla kez eklenemez."
                )
            )
        }

        if (
            recipientUserIds.any {
                it <= 0
            }
        ) {

            return Result.failure(
                IllegalArgumentException(
                    "Alıcı kullanıcı kimliği geçersizdir."
                )
            )
        }

        // ---------------------------------------------------------------------
        // SUBJECT
        // ---------------------------------------------------------------------

        if (
            subject.isBlank()
        ) {

            return Result.failure(
                IllegalArgumentException(
                    "Mesaj konusu zorunludur."
                )
            )
        }

        if (
            subject.length >
            MailboxRules.MAXIMUM_SUBJECT_LENGTH
        ) {

            return Result.failure(
                IllegalArgumentException(
                    "Mesaj konusu en fazla 250 karakter olabilir."
                )
            )
        }

        // ---------------------------------------------------------------------
        // BODY
        // ---------------------------------------------------------------------

        if (
            body.isBlank()
        ) {

            return Result.failure(
                IllegalArgumentException(
                    "Mesaj içeriği zorunludur."
                )
            )
        }

        if (
            body.length >
            MailboxRules.MAXIMUM_BODY_LENGTH
        ) {

            return Result.failure(
                IllegalArgumentException(
                    "Mesaj içeriği en fazla 20000 karakter olabilir."
                )
            )
        }

        // ---------------------------------------------------------------------
        // ATTACHMENTS
        // ---------------------------------------------------------------------

        return validateAttachmentsUseCase(
            attachments
        )
    }
}