package com.alperensarac.projectmanagementkotlin.domain.model.mailbox

/**
 * MailboxListQueryDto'nun domain karşılığı.
 */
data class MailboxFilter(
    val search: String? = null,

    /**
     * Inbox için anlamlıdır.
     *
     * null  -> hepsi
     * true  -> okunanlar
     * false -> okunmayanlar
     */
    val isRead: Boolean? = null,

    /**
     * null  -> hepsi
     * true  -> ekli mesajlar
     * false -> eki olmayanlar
     */
    val hasAttachment: Boolean? = null
)