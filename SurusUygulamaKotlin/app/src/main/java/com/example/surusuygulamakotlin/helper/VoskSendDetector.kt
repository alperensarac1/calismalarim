package com.example.surusuygulamakotlin.helper

import android.content.Context
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.json.JSONObject

class VoskSendDetector(
    private val context: Context,
    private val onSendDetected: () -> Unit
) : RecognitionListener {

    private var model: Model? = null
    private var speechService: SpeechService? = null

    private val sampleRate = 16000.0f
    private var lastTriggerAt = 0L
    private val cooldownMs = 5000L

    fun start(onStatus: (String) -> Unit, onError: (Throwable) -> Unit) {
        try {
            // assets/vosk-tr -> files/vosk-tr
            val modelDir = AssetModelCopier.copyAssetFolder(context, "vosk-tr", "vosk-tr")
            model = Model(modelDir.absolutePath)

            val recognizer = Recognizer(model, sampleRate)
            speechService = SpeechService(recognizer, sampleRate).also {
                it.startListening(this)
            }

            onStatus("Vosk dinliyor (offline) – 'gönder' bekleniyor")
        } catch (t: Throwable) {
            onError(t)
        }
    }

    fun stop() {
        try { speechService?.stop() } catch (_: Throwable) {}
        try { speechService?.shutdown() } catch (_: Throwable) {}
        speechService = null

        try { model?.close() } catch (_: Throwable) {}
        model = null
    }

    // ---- RecognitionListener ----
    override fun onPartialResult(hypothesis: String?) {
        if (hypothesis.isNullOrBlank()) return
        val partial = extractField(hypothesis, "partial") ?: return
        checkForSend(partial)
    }

    override fun onResult(hypothesis: String?) {
        if (hypothesis.isNullOrBlank()) return
        val text = extractField(hypothesis, "text") ?: return
        checkForSend(text)
    }

    override fun onFinalResult(hypothesis: String?) {
        // genelde onResult yeterli, istersen burayı da kullanabilirsin
    }

    override fun onError(exception: Exception?) {
        // Serviste handle edeceğiz (start() onError ile)
    }

    override fun onTimeout() {
        // gerekirse yeniden başlatılabilir
    }

    private fun extractField(json: String, key: String): String? {
        return try {
            val obj = JSONObject(json)
            obj.optString(key, null)
        } catch (_: Throwable) {
            null
        }
    }

    private fun checkForSend(raw: String) {
        val now = System.currentTimeMillis()
        if (now - lastTriggerAt < cooldownMs) return

        val normalized = raw
            .lowercase()
            .replace('ı', 'i')   // küçük normalize (bazı modellerde ı/i karışabiliyor)
            .replace('ğ', 'g')
            .replace('ş', 's')
            .replace('ö', 'o')
            .replace('ü', 'u')
            .replace('ç', 'c')

        // "gönder" kelimesi / benzer yazımlar
        if (normalized.contains("gonder")) {
            lastTriggerAt = now
            onSendDetected()
        }
    }
}
