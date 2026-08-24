package com.alperensarac.projectmanagementkotlin.feature.mailbox.compose

sealed interface MailboxComposeUiEvent {

    data class ShowMessage(
        val message: String
    ) : MailboxComposeUiEvent

    data class MessageSent(
        val message: String
    ) : MailboxComposeUiEvent
}