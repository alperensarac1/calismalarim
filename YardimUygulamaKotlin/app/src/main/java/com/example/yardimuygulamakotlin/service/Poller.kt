package com.example.yardimuygulamakotlin.service

// Poller.kt
import kotlinx.coroutines.*

class Poller(
    private val scope: CoroutineScope,
    private val intervalMs: Long,
    private val block: suspend () -> Unit
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try { block() } catch (_: Exception) {}
                delay(intervalMs)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
