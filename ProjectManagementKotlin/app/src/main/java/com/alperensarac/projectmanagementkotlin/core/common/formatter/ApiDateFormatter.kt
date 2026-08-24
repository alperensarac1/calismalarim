package com.alperensarac.projectmanagementkotlin.core.common.formatter

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/**
 * Kullanıcıya gösterilen tarih formatı ile backend ISO tarih formatı
 * arasındaki dönüşümü yapar.
 *
 * UI:
 *
 * 10.08.2026
 *
 * API:
 *
 * 2026-08-10T00:00:00.000Z
 */
class ApiDateFormatter @Inject constructor() {

    fun todayDisplayDate(): String {

        return createDisplayFormatter()
            .format(
                Calendar.getInstance().time
            )
    }

    /**
     * dd.MM.yyyy -> ISO UTC
     */
    fun displayDateToApiUtc(
        value: String
    ): String? {

        return try {

            val date =
                createDisplayFormatter()
                    .parse(value.trim())
                    ?: return null

            createApiFormatter()
                .format(date)

        } catch (_: ParseException) {

            null
        }
    }

    /**
     * Backend ISO değerinden yalnızca tarihi çıkartıp dialog'a basar.
     */
    fun apiDateToDisplayDate(
        value: String
    ): String {

        val normalized =
            value
                .trim()
                .take(10)

        val parser =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            ).apply {

                isLenient = false

                timeZone =
                    TimeZone.getTimeZone("UTC")
            }

        return runCatching {

            val date =
                parser.parse(normalized)
                    ?: return@runCatching value

            createDisplayFormatter()
                .format(date)

        }.getOrDefault(value)
    }

    private fun createDisplayFormatter():
            SimpleDateFormat {

        return SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        ).apply {

            isLenient = false
        }
    }

    private fun createApiFormatter():
            SimpleDateFormat {

        return SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        ).apply {

            timeZone =
                TimeZone.getTimeZone("UTC")
        }
    }
}