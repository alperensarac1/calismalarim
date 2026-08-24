package com.alperensarac.projectmanagementkotlin.feature.mailbox.compose

import com.alperensarac.projectmanagementkotlin.data.upload.MailboxSelectedFile
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRecipientUser
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRules

data class MailboxComposeUiState(

    // -------------------------------------------------------------------------
    // RECIPIENT SEARCH
    // -------------------------------------------------------------------------

    val recipientSearch: String =
        "",

    val isSearchingRecipients: Boolean =
        false,

    val recipientResults:
    List<MailboxRecipientUser> =
        emptyList(),

    val recipientSearchError: String? =
        null,

    // -------------------------------------------------------------------------
    // SELECTED RECIPIENTS
    // -------------------------------------------------------------------------

    val selectedRecipients:
    List<MailboxRecipientUser> =
        emptyList(),

    // -------------------------------------------------------------------------
    // MESSAGE
    // -------------------------------------------------------------------------

    val subject: String =
        "",

    val body: String =
        "",

    // -------------------------------------------------------------------------
    // ATTACHMENTS
    // -------------------------------------------------------------------------

    val attachments:
    List<MailboxSelectedFile> =
        emptyList(),

    // -------------------------------------------------------------------------
    // SEND
    // -------------------------------------------------------------------------

    val isSending: Boolean =
        false,

    val uploadProgress: Int =
        0
) {

    val recipientCount: Int
        get() =
            selectedRecipients.size

    val attachmentCount: Int
        get() =
            attachments.size

    val totalAttachmentBytes: Long
        get() =
            attachments.sumOf {
                it.sizeBytes
            }

    val canAddMoreRecipients: Boolean
        get() =
            recipientCount <
                    MailboxRules.MAXIMUM_RECIPIENT_COUNT

    val canAddMoreAttachments: Boolean
        get() =
            attachmentCount <
                    MailboxRules.MAXIMUM_ATTACHMENT_COUNT

    val canSend: Boolean
        get() =
            !isSending &&
                    selectedRecipients.isNotEmpty() &&
                    subject.isNotBlank() &&
                    body.isNotBlank()
}