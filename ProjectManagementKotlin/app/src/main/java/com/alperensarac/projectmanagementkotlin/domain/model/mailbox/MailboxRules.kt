package com.alperensarac.projectmanagementkotlin.domain.model.mailbox

/**
 * Backend:
 *
 * ProjectManagement.Application.Mailbox.MailboxFileConstants
 *
 * ile aynı kuralları Android tarafında temsil eder.
 *
 * ÖNEMLİ:
 * Asıl doğrulama kaynağı backend'dir.
 * Buradaki kurallar kullanıcıya isteği göndermeden önce
 * hızlı geri bildirim vermek içindir.
 */
object MailboxRules {

    /**
     * Tek dosya maksimum 200 MB.
     */
    const val MAXIMUM_FILE_SIZE_BYTES: Long =
        200L * 1024L * 1024L

    /**
     * Bütün attachment'ların toplamı maksimum 200 MB.
     */
    const val MAXIMUM_TOTAL_FILE_SIZE_BYTES: Long =
        200L * 1024L * 1024L

    /**
     * Tek mesaja en fazla 10 attachment.
     */
    const val MAXIMUM_ATTACHMENT_COUNT: Int =
        10

    /**
     * Tek mesaja en fazla 50 alıcı.
     */
    const val MAXIMUM_RECIPIENT_COUNT: Int =
        50

    /**
     * Subject maksimum 250 karakter.
     */
    const val MAXIMUM_SUBJECT_LENGTH: Int =
        250

    /**
     * Body maksimum 20.000 karakter.
     */
    const val MAXIMUM_BODY_LENGTH: Int =
        20_000

    /**
     * Backend attachment retention:
     *
     * 1 ay.
     */
    const val ATTACHMENT_RETENTION_MONTHS: Int =
        1

    val ALLOWED_EXTENSIONS: Set<String> =
        setOf(
            ".pdf",
            ".doc",
            ".docx",
            ".zip",
            ".png",
            ".jpg",
            ".jpeg"
        )

    val ALLOWED_CONTENT_TYPES: Set<String> =
        setOf(
            "application/pdf",

            "application/msword",

            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",

            "application/zip",
            "application/x-zip-compressed",
            "multipart/x-zip",

            "image/png",

            "image/jpeg",
            "image/jpg"
        )
}