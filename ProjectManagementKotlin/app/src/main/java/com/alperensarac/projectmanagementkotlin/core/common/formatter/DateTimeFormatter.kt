package com.alperensarac.projectmanagementkotlin.core.common.formatter

import android.annotation.SuppressLint
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Backend tarihlerini kullanıcı cihazının yerel saat dilimine dönüştürür.
 *
 * Backend DateTime değerlerinin iki farklı biçimde gelebildiği görüldü:
 *
 * 1.
 * 2026-07-26T14:17:49.068Z
 *
 * 2.
 * 2026-07-31T21:00:00
 *
 * İkinci format timezone bilgisi içermiyor.
 *
 * Backend sözleşmesine göre DateTime alanlarını UTC kabul ederek
 * ZoneOffset.UTC üzerinden yerel saate dönüştürüyoruz.
 */
class DateTimeFormatter @Inject constructor() {

    @SuppressLint("NewApi")
    private val displayFormatter =
        DateTimeFormatter.ofPattern(
            DISPLAY_PATTERN,
            Locale.getDefault()
        )

    @SuppressLint("NewApi")
    fun formatUtcDateTime(
        value: String?
    ): String {
        if (value.isNullOrBlank()) {
            return "-"
        }

        val instant =
            parseToInstant(value)
                ?: return value

        return instant
            .atZone(ZoneId.systemDefault())
            .format(displayFormatter)
    }

    /**
     * Çeşitli backend ISO formatlarını Instant'a dönüştürmeye çalışır.
     */
    @SuppressLint("NewApi")
    private fun parseToInstant(
        value: String
    ): Instant? {

        /*
         * Örnek:
         *
         * 2026-07-26T14:17:49.068Z
         */
        runCatching {
            return Instant.parse(value)
        }

        /*
         * Offset içeren başka ISO formatları.
         *
         * Örnek:
         *
         * 2026-08-05T12:56:56.3988516+00:00
         */
        runCatching {
            return OffsetDateTime
                .parse(value)
                .toInstant()
        }

        /*
         * Timezone bilgisi bulunmayan backend DateTime.
         *
         * Örnek:
         *
         * 2026-07-31T21:00:00
         *
         * Backend tarihlerini UTC kabul ettiğimiz için burada açıkça UTC
         * zone atanır.
         */
        runCatching {
            return LocalDateTime
                .parse(value)
                .toInstant(ZoneOffset.UTC)
        }

        return null
    }

    private companion object {
        const val DISPLAY_PATTERN = "dd.MM.yyyy HH:mm"
    }
}