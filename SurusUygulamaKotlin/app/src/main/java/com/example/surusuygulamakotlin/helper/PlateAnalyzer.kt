package com.example.surusuygulamakotlin.helper

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.surusuygulamakotlin.ml.MlKitPlateRecognizer
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class PlateAnalyzer(
    private val converter: YuvToRgbConverter,
    private val recognizer: MlKitPlateRecognizer,
    private val onDebug: (String) -> Unit = {}
) : ImageAnalysis.Analyzer {

    private val scope = CoroutineScope(Dispatchers.Default)

    private var lastRunAt = 0L
    private val minIntervalMs = 400L // OCR ağır → 2.5 fps

    private val lastSeen = ConcurrentHashMap<String, Long>()
    private val debounceMs = 4000L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastRunAt < minIntervalMs) {
            image.close()
            return
        }
        lastRunAt = now

        val mediaImage = image.image
        if (mediaImage == null) {
            image.close()
            return
        }

        val bmp = Bitmap.createBitmap(
            image.width,
            image.height,
            Bitmap.Config.ARGB_8888
        )

        try {
            converter.yuvToRgb(mediaImage, bmp)
        } catch (e: Throwable) {
            image.close()
            return
        }

        scope.launch {
            try {
                val plates = recognizer.recognize(bmp)
                for (p in plates) {
                    val norm = p.replace(" ", "")
                    val prev = lastSeen[norm] ?: 0L
                    if (now - prev < debounceMs) continue
                    lastSeen[norm] = now

                    PlateBuffer.add(p)
                    onDebug("MLKit plate: $p")
                }
            } catch (e: Throwable) {
                onDebug("OCR error: ${e.message}")
            } finally {
                image.close()
            }
        }
    }
}
