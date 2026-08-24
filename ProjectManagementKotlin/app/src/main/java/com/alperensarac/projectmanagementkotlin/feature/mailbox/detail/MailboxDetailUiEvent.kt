package com.alperensarac.projectmanagementkotlin.feature.mailbox.detail

sealed interface MailboxDetailUiEvent {

    data class ShowMessage(
        val message: String
    ) : MailboxDetailUiEvent

    /**
     * Inbox mesajı detay endpointi tarafından otomatik olarak
     * okundu hâline getirildi.
     */
    data object InboxMessageMarkedAsRead :
        MailboxDetailUiEvent

    data class MessageDeleted(
        val message: String
    ) : MailboxDetailUiEvent

    data class AttachmentDownloaded(
        val fileName: String
    ) : MailboxDetailUiEvent
}