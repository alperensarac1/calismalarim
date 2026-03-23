package com.example.surusuygulamakotlin.helper

import android.os.SystemClock
import java.util.concurrent.atomic.AtomicReference

object PlateStore {
    data class Last(val plate: String, val t: Long)

    private val last = AtomicReference<Last?>(null)

    fun set(plate: String) {
        val p = plate.trim()
        if (p.isBlank()) return
        last.set(Last(p, SystemClock.elapsedRealtime()))
    }

    fun getIfFresh(windowMs: Long = 30_000L): String? {
        val v = last.get() ?: return null
        val now = SystemClock.elapsedRealtime()
        return if (now - v.t <= windowMs) v.plate else null
    }

    fun clear() {
        last.set(null)
    }
}
