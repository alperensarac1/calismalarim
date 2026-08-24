package com.alperensarac.projectmanagementkotlin.feature.mailbox

/**
 * Mailbox ekranları arasında SavedStateHandle üzerinden
 * sonuç aktarırken kullanılan ortak key'ler.
 *
 * Neden ayrı bir object?
 *
 * Daha önce MailboxFragment:
 *
 * MailboxDetailFragment.RESULT_MAILBOX_CHANGED
 *
 * sabitini kullanıyordu.
 *
 * Fakat Compose ekranı da mailbox listesini değiştirebilir.
 *
 * Bu nedenle sonucu belirli bir Fragment'a ait yapmak yerine
 * feature seviyesinde ortak tutuyoruz.
 */
object MailboxNavigationResult {

    /**
     * Inbox/Sent listesinde backend'den tekrar veri çekilmesini ister.
     *
     * Şu işlemlerden sonra true gönderilebilir:
     *
     * - yeni mesaj gönderildi
     * - mesaj okundu
     * - mesaj okunmadı
     * - mesaj silindi
     * - Inbox mesajı detail ekranında açıldı
     */
    const val MAILBOX_CHANGED =
        "mailbox_changed"

    /**
     * Değişikliğin hangi mailbox klasöründe olduğunu belirtmek için
     * kullanılabilir.
     *
     * Şimdilik zorunlu değil ama ileride yalnızca gerekli klasörü
     * refresh etmek istediğimizde kullanabiliriz.
     */
    const val FOLDER =
        "mailbox_changed_folder"
}