package com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox

import com.alperensarac.projectmanagementkotlin.data.upload.MailboxSelectedFile
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRules
import javax.inject.Inject

/**
 * Backend UploadedMailboxFileValidator ve
 * SendMailboxMessageValidator attachment kurallarının
 * Android tarafındaki ön doğrulamasıdır.
 */
class ValidateMailboxAttachmentsUseCase @Inject constructor() {

    operator fun invoke(
        files: List<MailboxSelectedFile>
    ): Result<Unit> {

        // ---------------------------------------------------------------------
        // COUNT
        // ---------------------------------------------------------------------

        if (
            files.size >
            MailboxRules.MAXIMUM_ATTACHMENT_COUNT
        ) {

            return Result.failure(
                IllegalArgumentException(
                    "Bir mesaja en fazla " +
                            "${MailboxRules.MAXIMUM_ATTACHMENT_COUNT} " +
                            "dosya eklenebilir."
                )
            )
        }

        var totalSize =
            0L

        for (file in files) {

            // -----------------------------------------------------------------
            // FILE NAME
            // -----------------------------------------------------------------

            if (file.fileName.isBlank()) {

                return Result.failure(
                    IllegalArgumentException(
                        "Dosya adı boş olamaz."
                    )
                )
            }

            if (
                file.fileName.length >
                255
            ) {

                return Result.failure(
                    IllegalArgumentException(
                        "Dosya adı en fazla 255 karakter olabilir."
                    )
                )
            }

            // -----------------------------------------------------------------
            // SIZE
            // -----------------------------------------------------------------

            if (
                file.sizeBytes <=
                0L
            ) {

                return Result.failure(
                    IllegalArgumentException(
                        "${file.fileName}: Boş dosya yüklenemez."
                    )
                )
            }

            if (
                file.sizeBytes >
                MailboxRules
                    .MAXIMUM_FILE_SIZE_BYTES
            ) {

                return Result.failure(
                    IllegalArgumentException(
                        "${file.fileName}: Bir dosyanın boyutu 200 MB'ı geçemez."
                    )
                )
            }

            // -----------------------------------------------------------------
            // EXTENSION
            // -----------------------------------------------------------------

            if (
                file.extension.lowercase() !in
                MailboxRules.ALLOWED_EXTENSIONS
            ) {

                return Result.failure(
                    IllegalArgumentException(
                        "${file.fileName}: Yalnızca PDF, Word, ZIP, PNG, JPG ve JPEG dosyaları yüklenebilir."
                    )
                )
            }

            // -----------------------------------------------------------------
            // MIME
            // -----------------------------------------------------------------

            if (
                file.contentType.lowercase() !in
                MailboxRules.ALLOWED_CONTENT_TYPES
            ) {

                return Result.failure(
                    IllegalArgumentException(
                        "${file.fileName}: Dosyanın içerik türüne izin verilmiyor."
                    )
                )
            }

            // -----------------------------------------------------------------
            // TOTAL SIZE
            // -----------------------------------------------------------------

            /*
             * Backend'deki overflow-safe algoritmanın aynısı.
             *
             * totalSize + file.sizeBytes
             *
             * işlemini doğrudan yapmak yerine önce kalan kapasiteyi
             * karşılaştırıyoruz.
             */
            if (
                totalSize >
                MailboxRules
                    .MAXIMUM_TOTAL_FILE_SIZE_BYTES -
                file.sizeBytes
            ) {

                return Result.failure(
                    IllegalArgumentException(
                        "Eklenen dosyaların toplam boyutu 200 MB'ı geçemez."
                    )
                )
            }

            totalSize +=
                file.sizeBytes
        }

        return Result.success(
            Unit
        )
    }
}