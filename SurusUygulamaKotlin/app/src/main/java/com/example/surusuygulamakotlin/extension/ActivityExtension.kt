package com.example.surusuygulamakotlin.extension

import androidx.appcompat.app.AppCompatActivity
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.surusuygulamakotlin.helper.PlateDetectorHelper
import com.example.surusuygulamakotlin.helper.YuvToRgbConverter
import com.example.surusuygulamakotlin.ui.MlKitOcr
import com.example.surusuygulamakotlin.ui.OverlayView
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG_PREVIEW = "PLATE_PREVIEW"
private const val TAG_TEXT = "PLATE_TEXT"

private var actCameraProvider: ProcessCameraProvider? = null
private val actExecutor = Executors.newSingleThreadExecutor()
private var actConverter: YuvToRgbConverter? = null
private var actDetector: PlateDetectorHelper? = null
private var actOcr: MlKitOcr? = null

// OCR’yi çok sık çağırmayalım
private val ocrBusy = AtomicBoolean(false)

// ✅ OCR’dan çıkan son plaka (overlay label için)
@Volatile
private var lastPlateText: String? = null

@OptIn(ExperimentalGetImage::class)
fun AppCompatActivity.startPreviewPlateBoxes(
    previewView: PreviewView,
    overlayView: OverlayView,
    onStatus: (String) -> Unit
) {
    previewView.post {
        Log.e(TAG_PREVIEW, "startPreviewPlateBoxes() view=${previewView.width}x${previewView.height}")

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = future.get()
            actCameraProvider = provider

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Target rotation sync
            try { analysis.targetRotation = previewView.display.rotation } catch (_: Throwable) {}

            val converter = actConverter ?: YuvToRgbConverter(this).also { actConverter = it }
            val detector = actDetector ?: PlateDetectorHelper(this).also { actDetector = it }
            val ocr = actOcr ?: MlKitOcr().also { actOcr = it }

            analysis.setAnalyzer(actExecutor) { image ->
                val media = image.image ?: run { image.close(); return@setAnalyzer }
                val rotation = image.imageInfo.rotationDegrees

                val bmp0 = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                try {
                    converter.yuvToRgb(media, bmp0)
                } catch (t: Throwable) {
                    Log.e(TAG_PREVIEW, "yuvToRgb error: ${t.message}")
                    image.close()
                    return@setAnalyzer
                }

                // ✅ modeli doğru yönde besle
                val bmpForModel = if (rotation != 0) rotateBitmap(bmp0, rotation) else bmp0

                val dets = try {
                    detector.detect(bmpForModel, scoreThreshold = 0.25f)
                } catch (e: Throwable) {
                    Log.e(TAG_PREVIEW, "TFLite detect error: ${e.message}")
                    runOnUiThread { onStatus("TFLite hata: ${e.message}") }
                    emptyList()
                }

                // ✅ label: OCR varsa onu yaz, yoksa sadece score yaz
                val boxes: List<Pair<RectF, String?>> = dets.mapIndexed { idx, d ->
                    val plate = lastPlateText
                    val label = if (!plate.isNullOrBlank()) {
                        "$plate (%${(d.score * 100).toInt()})"
                    } else {
                        "PLAKA %${(d.score * 100).toInt()}"
                    }
                    d.rect to label
                }

                Log.e(
                    TAG_PREVIEW,
                    "rot=$rotation frame=${bmp0.width}x${bmp0.height} modelBmp=${bmpForModel.width}x${bmpForModel.height} boxes=${boxes.size}"
                )

                if (dets.isNotEmpty()) {
                    val best = dets.maxByOrNull { it.score }!!
                    val r = best.rect
                    Log.e(TAG_PREVIEW, "best rect=${r.left},${r.top},${r.right},${r.bottom} score=${best.score} lastPlate=${lastPlateText ?: "-"}")
                }

                // ✅ overlay
                runOnUiThread {
                    onStatus("Detections: ${boxes.size}")
                    overlayView.setResults(
                        imageWidth = bmpForModel.width,
                        imageHeight = bmpForModel.height,
                        viewWidth = previewView.width.coerceAtLeast(1),
                        viewHeight = previewView.height.coerceAtLeast(1),
                        boxes = boxes
                    )
                }

                // ✅ OCR logu: kutu varsa dene (çok sık çağırma)
                if (dets.isNotEmpty() && ocrBusy.compareAndSet(false, true)) {
                    val best = dets.maxByOrNull { it.score }!!
                    val crop = safeCrop(bmpForModel, best.rect)

                    if (crop == null) {
                        Log.e(TAG_TEXT, "CROP_NULL score=${best.score} rect=${best.rect}")
                        ocrBusy.set(false)
                    } else {
                        // OCR daha iyi olsun diye 2x büyüt
                        val up = Bitmap.createScaledBitmap(crop, crop.width * 2, crop.height * 2, true)

                        ocr.readPlateTextAsync(up) { plates: List<String> ->
                            if (plates.isEmpty()) {
                                Log.e(TAG_TEXT, "OCR_EMPTY score=${best.score} crop=${up.width}x${up.height}")
                            } else {
                                plates.forEach { p ->
                                    Log.e(TAG_TEXT, "PLATE=$p score=${best.score}")

                                    // ✅ EN KRİTİK: upload'ın kullanacağı buffer'a ekle
                                    com.example.surusuygulamakotlin.helper.PlateBuffer.add(p)
                                }
                            }
                            ocrBusy.set(false)
                        }

                    }
                }

                image.close()
            }

            provider.unbindAll()
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)

            Log.e(TAG_PREVIEW, "Camera bound ✅")
            onStatus("Önizleme başladı")
        }, ContextCompat.getMainExecutor(this))
    }
}

fun AppCompatActivity.stopPreviewPlateBoxes(
    previewView: PreviewView,
    overlayView: OverlayView
) {
    Log.e(TAG_PREVIEW, "stopPreviewPlateBoxes() called")
    try { actCameraProvider?.unbindAll() } catch (_: Throwable) {}
    actCameraProvider = null

    lastPlateText = null

    try {
        overlayView.setResults(
            imageWidth = 1,
            imageHeight = 1,
            viewWidth = previewView.width.coerceAtLeast(1),
            viewHeight = previewView.height.coerceAtLeast(1),
            boxes = emptyList()
        )
    } catch (_: Throwable) {}
}

private fun rotateBitmap(src: Bitmap, rotationDegrees: Int): Bitmap {
    if (rotationDegrees == 0) return src
    val m = android.graphics.Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
}

private fun safeCrop(src: Bitmap, r: RectF): Bitmap? {
    val padX = (r.width() * 0.12f)
    val padY = (r.height() * 0.25f)

    val left = (r.left - padX).toInt().coerceIn(0, src.width - 1)
    val top = (r.top - padY).toInt().coerceIn(0, src.height - 1)
    val right = (r.right + padX).toInt().coerceIn(left + 1, src.width)
    val bottom = (r.bottom + padY).toInt().coerceIn(top + 1, src.height)

    val w = right - left
    val h = bottom - top
    if (w < 30 || h < 18) return null
    return Bitmap.createBitmap(src, left, top, w, h)
}
