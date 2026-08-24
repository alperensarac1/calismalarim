package com.alperensarac.projectmanagementkotlin.domain.model.mailbox

/**
 * Mailbox domain katmanındaki kullanıcı modeli.
 */
data class MailboxUser(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String
)