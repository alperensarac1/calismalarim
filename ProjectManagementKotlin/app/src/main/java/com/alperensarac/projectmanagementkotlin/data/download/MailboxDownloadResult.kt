package com.alperensarac.projectmanagementkotlin.data.download

/**
 * Attachment indirme operasyonunun sonucudur.
 *
 * Domain AppResult'tan ayrı tutulmasının nedeni download işleminin
 * Android framework Uri/ContentResolver altyapısıyla ilişkili olmasıdır.
 */
sealed interface MailboxDownloadResult {

    data object Success :
        MailboxDownloadResult

    data class Error(
        val message: String
    ) : MailboxDownloadResult
}