package com.alperensarac.projectmanagementkotlin.domain.model.mailbox

/**
 * Android tarafındaki mailbox klasör türüdür.
 *
 * Backend enum'u değildir.
 *
 * Sadece hangi endpointin çağrılacağını belirlemek için
 * uygulama içinde kullanılır.
 */
enum class MailboxFolder {
    INBOX,
    SENT
}