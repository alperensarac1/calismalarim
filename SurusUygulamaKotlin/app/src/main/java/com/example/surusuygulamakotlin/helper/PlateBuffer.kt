package com.example.surusuygulamakotlin.helper

import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object PlateBuffer {

    private data class Seen(val plate: String, val t: Long)

    private val q = ConcurrentLinkedQueue<Seen>()
    private val lastSeenAt = ConcurrentHashMap<String, Long>()

    private const val WINDOW_MS = 30_000L
    private const val SAME_PLATE_DEBOUNCE_MS = 1500L

    @Synchronized
    fun add(raw: String) {
        val plate = raw.trim()
        if (plate.isBlank()) return

        val now = SystemClock.elapsedRealtime()
        purgeOld(now)

        val prev = lastSeenAt[plate] ?: 0L
        if (now - prev < SAME_PLATE_DEBOUNCE_MS) return

        lastSeenAt[plate] = now
        q.add(Seen(plate, now))

        // ✅ debug
        Log.e("PLATE_BUF", "ADD=$plate size=${q.size}")
    }

    @Synchronized
    fun snapshotLast30s(max: Int = 30): List<String> {
        val now = SystemClock.elapsedRealtime()
        purgeOld(now)

        val uniqLatest = HashMap<String, Long>()
        q.forEach { uniqLatest[it.plate] = it.t }

        return uniqLatest.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(max)
    }

    @Synchronized
    fun snapshotAndClear(max: Int = 30): List<String> {
        val list = snapshotLast30s(max)
        q.clear()
        return list
    }

    private fun purgeOld(now: Long) {
        while (true) {
            val head = q.peek() ?: break
            if (now - head.t <= WINDOW_MS) break
            q.poll()
        }

        val it = lastSeenAt.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (now - e.value > WINDOW_MS) it.remove()
        }
    }
}
