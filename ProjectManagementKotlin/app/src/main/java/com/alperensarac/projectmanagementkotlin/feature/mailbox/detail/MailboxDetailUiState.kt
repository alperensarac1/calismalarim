package com.alperensarac.projectmanagementkotlin.feature.mailbox.detail

import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessageDetail

/**
 * Mailbox detay ekranının bütün kalıcı UI state'i.
 */
data class MailboxDetailUiState(

    // -------------------------------------------------------------------------
    // MESSAGE
    // -------------------------------------------------------------------------

    val isLoading: Boolean =
        false,

    val message: MailboxMessageDetail? =
        null,

    // -------------------------------------------------------------------------
    // MESSAGE OPERATIONS
    // -------------------------------------------------------------------------

    val isOperationRunning: Boolean =
        false,

    // -------------------------------------------------------------------------
    // ATTACHMENT DOWNLOAD
    // -------------------------------------------------------------------------

    /**
     * Şu anda hangi attachment indiriliyor?
     *
     * null:
     * download yok.
     */
    val downloadingAttachmentId: Int? =
        null,

    /**
     * 0..100.
     *
     * Sunucu Content-Length döndürmezse bazı aşamalarda null kalabilir.
     */
    val downloadProgress: Int? =
        null,

    // -------------------------------------------------------------------------
    // ERROR
    // -------------------------------------------------------------------------

    val errorMessage: String? =
        null
) {

    val hasContent: Boolean
        get() =
            message != null

    /**
     * Aynı anda ikinci attachment download başlatılmasını engeller.
     */
    val isDownloading: Boolean
        get() =
            downloadingAttachmentId != null
}