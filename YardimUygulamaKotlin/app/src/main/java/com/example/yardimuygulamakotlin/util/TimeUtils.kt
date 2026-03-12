package com.example.yardimuygulamakotlin.util

import java.util.Locale

object TimeUtils {
    fun formatRemainingSeconds(sec: Int): String {
        val s = if (sec < 0) 0 else sec
        val mm = s / 60
        val ss = s % 60
        return String.format(Locale.US, "%02d:%02d", mm, ss)
    }
}