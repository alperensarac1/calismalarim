package com.alperensarac.projectmanagementkotlin.domain.model.mailbox

/**
 * Yeni mesaj ekranında seçilebilecek kullanıcı.
 */
data class MailboxRecipientUser(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,
    val isActive: Boolean
)