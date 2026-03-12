package com.example.yardimuygulamajetpack.util

object TimeUtils {
    fun formatRemainingSeconds(sec: Int): String {
        val s = sec.coerceAtLeast(0)
        val mm = s / 60
        val ss = s % 60
        return "%02d:%02d".format(mm, ss)
    }
}