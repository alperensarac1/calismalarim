package com.alperensarac.projectmanagementkotlin.data.upload

import android.net.Uri

/**
 * Android Storage Access Framework üzerinden seçilmiş
 * bir attachment'ı temsil eder.
 *
 * Dosya içeriğini burada ByteArray olarak TUTMUYORUZ.
 *
 * Sadece:
 *
 * - Uri
 * - dosya adı
 * - MIME
 * - boyut
 *
 * metadata bilgilerini tutuyoruz.
 */
data class MailboxSelectedFile(

    val uri: Uri,

    val fileName: String,

    val contentType: String,

    val sizeBytes: Long
) {

    val extension: String
        get() {

            val dotIndex =
                fileName.lastIndexOf('.')

            if (
                dotIndex < 0 ||
                dotIndex ==
                fileName.lastIndex
            ) {
                return ""
            }

            return fileName
                .substring(dotIndex)
                .lowercase()
        }
}