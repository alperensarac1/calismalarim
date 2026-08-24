package com.alperensarac.projectmanagementkotlin.domain.model.mailbox

/**
 * Mailbox upload progress state.
 */
data class MailboxUploadProgress(
    val uploadedBytes: Long,
    val totalBytes: Long
) {

    val percentage: Int
        get() {

            if (
                totalBytes <=
                0L
            ) {
                return 0
            }

            return (
                    uploadedBytes *
                            100L /
                            totalBytes
                    )
                .toInt()
                .coerceIn(
                    0,
                    100
                )
        }
}