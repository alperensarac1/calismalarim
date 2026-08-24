package com.alperensarac.projectmanagementkotlin.data.upload

import android.content.ContentResolver
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxUploadProgress
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * SendMailboxMessageRequest için multipart body oluşturur.
 */
@Singleton
class MailboxMultipartBuilder @Inject constructor() {

    fun build(
        contentResolver: ContentResolver,
        recipientUserIds: List<Int>,
        subject: String,
        body: String,
        attachments: List<MailboxSelectedFile>,
        onProgress: (MailboxUploadProgress) -> Unit
    ): List<MultipartBody.Part> {

        val parts =
            mutableListOf<MultipartBody.Part>()

        // ---------------------------------------------------------------------
        // RECIPIENT USER IDS
        // ---------------------------------------------------------------------

        /*
         * ASP.NET model:
         *
         * List<int> RecipientUserIds
         *
         * Multipart:
         *
         * RecipientUserIds = 2
         * RecipientUserIds = 5
         * RecipientUserIds = 8
         *
         * şeklinde aynı field adı tekrar gönderilir.
         */
        recipientUserIds.forEach { userId ->

            parts +=
                MultipartBody.Part.createFormData(
                    "RecipientUserIds",
                    userId.toString()
                )
        }

        // ---------------------------------------------------------------------
        // SUBJECT
        // ---------------------------------------------------------------------

        parts +=
            MultipartBody.Part.createFormData(
                "Subject",
                null,
                subject.toRequestBody(
                    TEXT_MEDIA_TYPE
                )
            )

        // ---------------------------------------------------------------------
        // BODY
        // ---------------------------------------------------------------------

        parts +=
            MultipartBody.Part.createFormData(
                "Body",
                null,
                body.toRequestBody(
                    TEXT_MEDIA_TYPE
                )
            )

        // ---------------------------------------------------------------------
        // ATTACHMENTS
        // ---------------------------------------------------------------------

        val totalFileBytes =
            attachments.sumOf {
                it.sizeBytes
            }

        var uploadedBytes =
            0L

        var lastPercentage =
            -1

        attachments.forEach { file ->

            val requestBody =
                ContentUriRequestBody(
                    contentResolver =
                    contentResolver,

                    uri =
                    file.uri,

                    contentType =
                    file.contentType
                        .toMediaType(),

                    contentLength =
                    file.sizeBytes,

                    onBytesWritten = { written ->

                        uploadedBytes +=
                            written

                        val progress =
                            MailboxUploadProgress(
                                uploadedBytes =
                                uploadedBytes,

                                totalBytes =
                                totalFileBytes
                            )

                        /*
                         * StateFlow'u her 64 KB chunk'ta gereksiz yere
                         * güncellememek için yalnızca yüzde değişiminde
                         * callback yapıyoruz.
                         */
                        if (
                            progress.percentage !=
                            lastPercentage
                        ) {

                            lastPercentage =
                                progress.percentage

                            onProgress(
                                progress
                            )
                        }
                    }
                )

            parts +=
                MultipartBody.Part.createFormData(
                    "Attachments",
                    file.fileName,
                    requestBody
                )
        }

        return parts
    }

    private companion object {

        val TEXT_MEDIA_TYPE =
            "text/plain; charset=utf-8"
                .toMediaType()
    }
}