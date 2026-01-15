package com.example.runneroyunjetpack.game_engine

import android.view.SurfaceHolder

class GameLoop(
    private val holder: SurfaceHolder,
    private val update: (Float) -> Unit,
    private val render: () -> Unit
) : Runnable {

    @Volatile private var running = false
    private var thread: Thread? = null

    fun start() {
        if (running) return
        running = true
        thread = Thread(this).also { it.start() }
    }

    fun stop() {
        running = false
        try { thread?.join() } catch (_: InterruptedException) {}
        thread = null
    }

    override fun run() {
        var lastNs = System.nanoTime()

        while (running) {
            if (!holder.surface.isValid) continue

            val now = System.nanoTime()
            var dt = (now - lastNs) / 1_000_000_000f
            if (dt > 0.033f) dt = 0.033f
            lastNs = now

            update(dt)
            render()
        }
    }
}
