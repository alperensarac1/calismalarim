package com.example.surusuygulamakotlin.helper

import android.annotation.SuppressLint
import android.media.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.sqrt

class KeywordSpotter(
    private val sampleRate: Int = 16000,
    private val triggerCooldownMs: Long = 5000L, // yanlış tetikleme için bekleme
    private val onTrigger: () -> Unit
) {
    private val running = AtomicBoolean(false)
    private var audioRecord: AudioRecord? = null
    private var lastTriggerAt = 0L

    // Placeholder eşik (RMS) – cihazına göre ayarlanır
    private val rmsThreshold = 0.06f

    @SuppressLint("MissingPermission")
    fun start() {
        if (running.getAndSet(true)) return

        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuf * 2
        )

        audioRecord?.startRecording()

        Thread {
            val buffer = ShortArray(minBuf)
            while (running.get()) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    // 1) Placeholder: RMS yüksekse tetikle
                    val rms = computeRms(buffer, read)
                    val now = System.currentTimeMillis()

                    if (rms > rmsThreshold && now - lastTriggerAt > triggerCooldownMs) {
                        // Burada ileride: TFLite inference ile "gonder" confidence kontrol edeceğiz.
                        lastTriggerAt = now
                        onTrigger()
                    }
                }
            }
            stopInternal()
        }.start()
    }

    fun stop() {
        running.set(false)
    }

    private fun stopInternal() {
        try {
            audioRecord?.stop()
        } catch (_: Throwable) {}
        try {
            audioRecord?.release()
        } catch (_: Throwable) {}
        audioRecord = null
    }

    private fun computeRms(buf: ShortArray, n: Int): Float {
        var sum = 0.0
        for (i in 0 until n) {
            val v = buf[i].toDouble() / 32768.0
            sum += v * v
        }
        return sqrt((sum / n).coerceAtLeast(0.0)).toFloat()
    }
}
