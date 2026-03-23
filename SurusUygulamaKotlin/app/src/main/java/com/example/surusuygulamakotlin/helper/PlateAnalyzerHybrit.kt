package com.example.surusuygulamakotlin.helper

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.RectF
import android.os.SystemClock
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.surusuygulamakotlin.ui.MlKitOcr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class PlateAnalyzerHybrid(
    private val converter: YuvToRgbConverter?,          // ✅ artık nullable: RGBA çalışırsa hiç gerek yok
    private val detector: PlateDetectorHelper,
    private val ocr: MlKitOcr,
    private val onDebug: (String) -> Unit = {},
    private val onOverlay: (imgW: Int, imgH: Int, boxes: List<Pair<RectF, String?>>) -> Unit =
        { _, _, _ -> }
) : ImageAnalysis.Analyzer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val TAG = "PLATE_PIPE"

    @Volatile private var lastPlateText: String? = null
    private var lastPlateSavedAt = 0L
    private val lastPlateTtlMs = 10_000L

    private var lastRunAt = 0L
    private val minIntervalMs = 350L

    private val lastSeen = ConcurrentHashMap<String, Long>()
    private val debounceMs = 2500L

    private var lastOcrAt = 0L
    private val ocrIntervalMs = 900L
    private val ocrBusy = AtomicBoolean(false)

    private val topK = 5
    private val minScoreForOcr = 0.15f

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastRunAt < minIntervalMs) {
            image.close()
            return
        }
        lastRunAt = now

        val rotation = image.imageInfo.rotationDegrees

        // ✅ 1) RGBA-first (S20FE fix)
        val bmp = tryMakeBitmapFromRgba(image)
            ?: run {
                // ✅ 2) fallback: eski YUV yolu
                val mediaImage = image.image
                if (mediaImage == null) {
                    image.close()
                    return
                }
                val b = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                try {
                    converter?.yuvToRgb(mediaImage, b)
                } catch (t: Throwable) {
                    Log.e(TAG, "yuvToRgb error: ${t.message}")
                    image.close()
                    return
                }
                b
            }

        image.close()

        scope.launch {
            try {
                val ts = SystemClock.elapsedRealtime()
                val bmpForModel = if (rotation != 0) rotateBitmap(bmp, rotation) else bmp

                val dets = detector.detect(bmpForModel, scoreThreshold = 0.10f)

                if (dets.isEmpty()) {
                    onOverlay(bmpForModel.width, bmpForModel.height, emptyList())
                    return@launch
                }

                val labelNow = lastPlateText
                onOverlay(
                    bmpForModel.width,
                    bmpForModel.height,
                    dets.map { it.rect to labelNow }
                )

                val doOcr = (ts - lastOcrAt) > ocrIntervalMs
                if (!doOcr) return@launch

                if (!ocrBusy.compareAndSet(false, true)) return@launch
                lastOcrAt = ts

                val top = dets
                    .filter { it.score >= minScoreForOcr }
                    .sortedByDescending { it.score }
                    .take(topK)

                if (top.isEmpty()) {
                    ocrBusy.set(false)
                    return@launch
                }

                val crops = mutableListOf<Pair<Float, Bitmap>>()
                for (d in top) {
                    val crop = safeCrop(bmpForModel, d.rect) ?: continue
                    val up = Bitmap.createScaledBitmap(crop, crop.width * 2, crop.height * 2, true)
                    crops += d.score to up
                }

                if (crops.isEmpty()) {
                    ocrBusy.set(false)
                    return@launch
                }

                fun runOcrAt(index: Int) {
                    if (index >= crops.size) {
                        ocrBusy.set(false)
                        return
                    }

                    val (score, cropBmp) = crops[index]

                    ocr.readPlateTextAsync(cropBmp) { plates: List<String> ->
                        try {
                            Log.e(TAG, "OCR plates.size=${plates.size} score=$score")

                            if (plates.isNotEmpty()) {
                                val tNow = SystemClock.elapsedRealtime()
                                for (raw in plates) {
                                    val norm = normalizePlate(raw)
                                    if (norm.isBlank()) continue

                                    val last = lastSeen[norm] ?: 0L
                                    if (tNow - last < debounceMs) continue
                                    lastSeen[norm] = tNow

                                    lastPlateText = norm
                                    lastPlateSavedAt = tNow

                                    PlateBuffer.add(norm)
                                    PlateStore.set(norm)

                                    Log.e(TAG, "PLATE=$norm score=$score ✅ (raw=$raw)")
                                    onDebug("PLATE=$norm score=$score")
                                }
                            } else {
                                val tNow = SystemClock.elapsedRealtime()
                                val cached = lastPlateText
                                if (cached != null && (tNow - lastPlateSavedAt) <= lastPlateTtlMs) {
                                    PlateBuffer.add(cached)
                                    Log.e(TAG, "OCR_EMPTY -> fallback cached=$cached score=$score")
                                } else {
                                    Log.e(TAG, "OCR_EMPTY score=$score")
                                }
                            }
                        } catch (e: Throwable) {
                            Log.e(TAG, "OCR callback error: ${e.message}")
                        } finally {
                            runOcrAt(index + 1)
                        }
                    }
                }

                runOcrAt(0)

            } catch (e: Throwable) {
                Log.e(TAG, "Hybrid error: ${e.message}")
                onDebug("Hybrid error: ${e.message}")
                ocrBusy.set(false)
            }
        }
    }

    // ✅ RGBA bitmap builder (debugte çalışan)
    private fun tryMakeBitmapFromRgba(image: ImageProxy): Bitmap? {
        return try {
            val p0 = image.planes.firstOrNull() ?: return null
            val buffer = p0.buffer ?: return null
            val width = image.width
            val height = image.height
            val rowStride = p0.rowStride
            val pixelStride = p0.pixelStride
            if (pixelStride <= 0) return null

            buffer.rewind()
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val expectedRow = width * pixelStride

            if (rowStride == expectedRow) {
                bmp.copyPixelsFromBuffer(buffer)
                bmp
            } else {
                val row = ByteArray(expectedRow)
                val dst = ByteArray(width * height * pixelStride)
                var dstOff = 0
                for (y in 0 until height) {
                    buffer.get(row, 0, expectedRow)
                    System.arraycopy(row, 0, dst, dstOff, expectedRow)
                    dstOff += expectedRow
                    val skip = rowStride - expectedRow
                    if (skip > 0) buffer.position(buffer.position() + skip)
                }
                bmp.copyPixelsFromBuffer(ByteBuffer.wrap(dst))
                bmp
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun normalizePlate(raw: String?): String {
        val s0 = raw?.trim().orEmpty()
        if (s0.isBlank()) return ""

        var s = s0.uppercase(Locale.ROOT)
        s = s.replace('İ', 'I')
            .replace('Ğ', 'G')
            .replace('Ü', 'U')
            .replace('Ş', 'S')
            .replace('Ö', 'O')
            .replace('Ç', 'C')

        s = s.replace(" ", "").replace("-", "").replace("_", "")
        s = s.replace(Regex("[^A-Z0-9]"), "")
        if (s.length !in 5..16) return ""
        return s
    }

    private fun safeCrop(src: Bitmap, r: RectF): Bitmap? {
        val padX = r.width() * 0.12f
        val padY = r.height() * 0.25f

        val left = (r.left - padX).toInt().coerceIn(0, src.width - 1)
        val top = (r.top - padY).toInt().coerceIn(0, src.height - 1)
        val right = (r.right + padX).toInt().coerceIn(left + 1, src.width)
        val bottom = (r.bottom + padY).toInt().coerceIn(top + 1, src.height)

        val w = right - left
        val h = bottom - top
        if (w < 20 || h < 20) return null
        return Bitmap.createBitmap(src, left, top, w, h)
    }

    private fun rotateBitmap(src: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return src
        val m = android.graphics.Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }
}
