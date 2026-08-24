package com.alperensarac.projectmanagementkotlin.feature.mailbox.list

import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFolder

/**
 * Mailbox liste ekranının kalıcı UI state'i.
 *
 * Gelen Kutusu ve Gönderilenler aynı Fragment içerisinde gösteriliyor.
 */
data class MailboxListUiState(

    /**
     * Şu anda hangi mailbox klasörü görüntüleniyor?
     */
    val folder: MailboxFolder =
        MailboxFolder.INBOX,

    /**
     * Arama metni.
     */
    val search: String = "",

    /**
     * Okundu filtresi.
     *
     * Backend dokümantasyonuna göre özellikle Inbox için anlamlıdır.
     */
    val readFilter: MailboxReadFilter =
        MailboxReadFilter.ALL,

    /**
     * Attachment filtresi.
     */
    val attachmentFilter: MailboxAttachmentFilter =
        MailboxAttachmentFilter.ALL
)

/**
 * UI tarafındaki okundu filtresi.
 *
 * Bu backend enum'u değildir.
 */
enum class MailboxReadFilter {

    ALL,

    READ,

    UNREAD;

    fun toApiValue(): Boolean? {

        return when (this) {

            ALL ->
                null

            READ ->
                true

            UNREAD ->
                false
        }
    }
}

/**
 * UI tarafındaki attachment filtresi.
 */
enum class MailboxAttachmentFilter {

    ALL,

    WITH_ATTACHMENT,

    WITHOUT_ATTACHMENT;

    fun toApiValue(): Boolean? {

        return when (this) {

            ALL ->
                null

            WITH_ATTACHMENT ->
                true

            WITHOUT_ATTACHMENT ->
                false
        }
    }
}