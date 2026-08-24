package com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox

import android.content.ContentResolver
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.data.upload.MailboxSelectedFile
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessageDetail
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxUploadProgress
import com.alperensarac.projectmanagementkotlin.domain.repository.MailboxRepository
import javax.inject.Inject

/**
 * Yeni mailbox mesajı gönderme use case'i.
 */
class SendMailboxMessageUseCase @Inject constructor(
    private val repository:
    MailboxRepository,

    private val validateMailboxMessageUseCase:
    ValidateMailboxMessageUseCase
) {

    suspend operator fun invoke(
        contentResolver: ContentResolver,
        recipientUserIds: List<Int>,
        subject: String,
        body: String,
        attachments: List<MailboxSelectedFile>,
        onUploadProgress: (MailboxUploadProgress) -> Unit
    ): AppResult<MailboxMessageDetail> {

        /*
         * Backend request göndermeden önce aynı business
         * kurallarını Android'de kontrol ediyoruz.
         */
        val validation =
            validateMailboxMessageUseCase(
                recipientUserIds =
                recipientUserIds,

                subject =
                subject,

                body =
                body,

                attachments =
                attachments
            )

        validation.exceptionOrNull()
            ?.let { throwable ->

                /*
                 * Burada mevcut NetworkError modeline yeni Validation
                 * üretmek yerine exception'ı caller tarafında
                 * compose form validator olarak kullanacağız.
                 *
                 * Asıl API hatası yine repository tarafından map edilir.
                 */
                throw IllegalArgumentException(
                    throwable.message
                        ?: "Mesaj bilgileri geçersiz."
                )
            }

        return repository.sendMessage(
            contentResolver =
            contentResolver,

            /*
             * Hem Android hem backend duplicate istemiyor.
             *
             * Burada distinct() yapmıyoruz.
             * Çünkü yanlış state oluştuysa validator'ın bunu yakalamasını
             * istiyoruz.
             */
            recipientUserIds =
            recipientUserIds,

            subject =
            subject.trim(),

            body =
            body.trim(),

            attachments =
            attachments,

            onUploadProgress =
            onUploadProgress
        )
    }
}