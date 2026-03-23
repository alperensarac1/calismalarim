package com.example.surusuygulamakotlin.test

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Size
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.surusuygulamakotlin.helper.PlateDetectorHelper
import com.example.surusuygulamakotlin.helper.UploadHelper
import com.example.surusuygulamakotlin.service.ApiClient
import com.example.surusuygulamakotlin.ui.MlKitOcr
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class DebugUploadActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var dbgOverlay: DebugOverlayView // ✅ overlay ismi yok
    private lateinit var tv: TextView
    private lateinit var btnRec: Button
    private lateinit var btnUpload: Button
    private lateinit var btnResume: Button

    @Volatile private var uiFrozen = false
    private val exec = Executors.newSingleThreadExecutor()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var provider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    private var detector: PlateDetectorHelper? = null
    private var ocr: MlKitOcr? = null

    private val busy = AtomicBoolean(false)
    private var lastOcrAt = 0L
    private val ocrIntervalMs = 700L

    private val seen = LinkedHashSet<String>()
    private var lastRecordedUri: Uri? = null
    private var lastPlatesJsonSent: String = "[]"

    private val REQ = 9100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---------------- UI (SAFE) ----------------
        previewView = PreviewView(this)

        dbgOverlay = DebugOverlayView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            isClickable = false
            isFocusable = false
        }

        tv = TextView(this).apply {
            textSize = 13f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0x88000000.toInt())
            setTextColor(0xFFFFFFFF.toInt())
        }

        btnRec = Button(this).apply { text = "REC 10s" }
        btnUpload = Button(this).apply { text = "UPLOAD TEST" }
        btnResume = Button(this).apply { text = "RESUME DEBUG" }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0x66000000)
            addView(btnRec, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(btnUpload, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(btnResume, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        // ✅ root’u “val root = … apply { addView }” şeklinde değil,
        // tek tek ekleyerek kuruyoruz (çakışma ihtimali yok)
        val frame = FrameLayout(this)
        frame.addView(
            previewView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        frame.addView(dbgOverlay) // ✅ View, ViewGroupOverlay değil
        frame.addView(
            tv,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(topBar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(frame, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        }

        setContentView(container)
        // ------------------------------------------------------------

        detector = PlateDetectorHelper(this)
        ocr = MlKitOcr()

        btnRec.setOnClickListener { startRec10s() }
        btnUpload.setOnClickListener { uploadLast() }
        btnResume.setOnClickListener {
            unfreezeUi()
            post("DEBUG resumed ✅")
        }

        ensurePermsThenStart()
    }

    override fun onDestroy() {
        try { activeRecording?.stop() } catch (_: Throwable) {}
        try { provider?.unbindAll() } catch (_: Throwable) {}
        try { exec.shutdown() } catch (_: Throwable) {}
        try { detector?.close() } catch (_: Throwable) {}
        detector = null
        ocr = null
        scope.cancel()
        super.onDestroy()
    }

    // ------------------------------------------------------------
    // Permissions
    // ------------------------------------------------------------

    private fun requiredPerms(): Array<String> {
        return arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    private fun hasAllPerms(): Boolean =
        requiredPerms().all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

    private fun ensurePermsThenStart() {
        if (hasAllPerms()) startCamera()
        else ActivityCompat.requestPermissions(this, requiredPerms(), REQ)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ && hasAllPerms()) startCamera()
    }

    // ------------------------------------------------------------
    // Camera
    // ------------------------------------------------------------

    private fun startCamera() {
        val fut = ProcessCameraProvider.getInstance(this)
        fut.addListener({
            provider = fut.get()
            val p = provider ?: return@addListener

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            analysis.setAnalyzer(exec, Analyzer())

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            p.unbindAll()
            p.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis, videoCapture)

            post("DEBUG UPLOAD READY ✅\n- RGBA analysis\n- detect + OCR\n- REC 10s ile plaka topla, sonra UPLOAD TEST")
        }, ContextCompat.getMainExecutor(this))
    }

    private inner class Analyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            val now = SystemClock.elapsedRealtime()
            if (!busy.compareAndSet(false, true)) {
                image.close(); return
            }
            try {
                val w = image.width
                val h = image.height
                val rot = image.imageInfo.rotationDegrees

                val bmp = makeBitmapFromRgba(image) ?: run {
                    image.close(); busy.set(false); return
                }
                image.close()

                val dets = try {
                    detector?.detect(bmp, scoreThreshold = 0.10f).orEmpty()
                } catch (_: Throwable) {
                    emptyList()
                }

                runOnUiThread {
                    dbgOverlay.setDetections(
                        imageW = w,
                        imageH = h,
                        rot = rot,
                        boxes = dets.map { it.rect to it.score }
                    )
                }

                if (now - lastOcrAt < ocrIntervalMs) {
                    updateText(dets.size, null, null)
                    return
                }
                lastOcrAt = now

                val best = dets.maxByOrNull { it.score }
                if (best == null) {
                    updateText(0, null, null)
                    return
                }

                val crop = safeCrop(bmp, best.rect) ?: run {
                    updateText(dets.size, null, null); return
                }
                val up = Bitmap.createScaledBitmap(crop, crop.width * 2, crop.height * 2, true)

                ocr?.readPlateTextAsync(up) { plates ->
                    val raw = plates.firstOrNull()
                    val norm = normalizePlate(raw)
                    if (norm.isNotBlank()) seen.add(norm)
                    updateText(dets.size, raw, norm)
                }

            } finally {
                busy.set(false)
            }
        }
    }

    // ------------------------------------------------------------
    // Record & Upload
    // ------------------------------------------------------------

    private fun startRec10s() {
        val vc = videoCapture ?: run {
            Toast.makeText(this, "VideoCapture yok", Toast.LENGTH_SHORT).show()
            return
        }
        if (activeRecording != null) {
            Toast.makeText(this, "Zaten kayıt var", Toast.LENGTH_SHORT).show()
            return
        }

        seen.clear()
        lastRecordedUri = null
        lastPlatesJsonSent = "[]"
        unfreezeUi()

        val file = File(cacheDir, "dbg_${System.currentTimeMillis()}.mp4")
        val out = FileOutputOptions.Builder(file).build()

        post("REC başladı… (10s)\nOCR ile plaka toplanıyor…")

        activeRecording = vc.output
            .prepareRecording(this, out)
            .apply {
                if (ContextCompat.checkSelfPermission(this@DebugUploadActivity, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
                ) withAudioEnabled()
            }
            .start(ContextCompat.getMainExecutor(this)) { ev ->
                when (ev) {
                    is VideoRecordEvent.Start -> {
                        scope.launch {
                            delay(10_000L)
                            try { activeRecording?.stop() } catch (_: Throwable) {}
                        }
                    }

                    is VideoRecordEvent.Finalize -> {
                        activeRecording = null
                        if (ev.hasError()) {
                            post("REC finalize error: ${ev.error} ${ev.cause?.message}")
                            return@start
                        }

                        val uri = Uri.fromFile(file)
                        lastRecordedUri = uri

                        val list = seen.toList()
                        val safe = if (list.isEmpty()) listOf("UNKNOWN") else list
                        lastPlatesJsonSent = Gson().toJson(safe)

                        post(
                            "REC bitti ✅\n" +
                                    "video=$uri\n" +
                                    "plates.size=${safe.size}\n" +
                                    "plates_json=$lastPlatesJsonSent\n\n" +
                                    "Şimdi UPLOAD TEST'e bas."
                        )
                    }
                }
            }
    }

    private fun uploadLast() {
        val uri = lastRecordedUri ?: run {
            Toast.makeText(this, "Önce REC 10s yap", Toast.LENGTH_SHORT).show()
            return
        }

        freezeUi("UPLOAD hazırlanıyor…\nvideo=$uri\nSENT plates_json=$lastPlatesJsonSent")

        scope.launch {
            try {
                val resultText = withContext(Dispatchers.IO) {
                    withTimeout(90_000L) {
                        val tempFile = File(uri.path!!)
                        val videoPart = UploadHelper.buildVideoPart(tempFile)

                        val textType = "text/plain".toMediaType()
                        val nowStr = getNowIstanbulString()

                        val resp = ApiClient.api.uploadReport(
                            video = videoPart,
                            platesJson = lastPlatesJsonSent.toRequestBody(textType),
                            clientSentAt = nowStr.toRequestBody(textType),
                            deviceLat = "".toRequestBody(textType),
                            deviceLng = "".toRequestBody(textType),
                            deviceAcc = "".toRequestBody(textType),
                            deviceLocAt = nowStr.toRequestBody(textType)
                        )

                        "UPLOAD DONE ✅\n" +
                                "SENT plates_json=$lastPlatesJsonSent\n\n" +
                                "SERVER:\n$resp"
                    }
                }
                // upload sonucu kilitte bile görünsün:
                runOnUiThread { tv.text = resultText }

            } catch (e: TimeoutCancellationException) {
                runOnUiThread { tv.text = "UPLOAD TIMEOUT ❌ (90sn)\nSENT plates_json=$lastPlatesJsonSent" }
            } catch (e: Throwable) {
                runOnUiThread { tv.text = "UPLOAD FAIL ❌ ${e.javaClass.simpleName}:${e.message}\nSENT plates_json=$lastPlatesJsonSent" }
            }
        }
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private fun updateText(detCount: Int, raw: String?, norm: String?) {
        val platesNow = seen.toList()
        val msg = buildString {
            appendLine("DEBUG UPLOAD")
            appendLine("det_count=$detCount")
            appendLine("ocr_raw=${raw ?: "-"}")
            appendLine("ocr_norm=${norm ?: "-"}")
            appendLine("seen.size=${platesNow.size}")
            if (platesNow.isNotEmpty()) appendLine("seen=$platesNow")
            appendLine()
            appendLine("REC 10s -> plates_json hazırlanır")
        }
        post(msg)
    }

    private fun post(msg: String) {
        runOnUiThread {
            if (uiFrozen) return@runOnUiThread
            tv.text = msg
        }
    }

    private fun makeBitmapFromRgba(image: ImageProxy): Bitmap? {
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

        return if (rowStride == expectedRow) {
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
    }

    private fun safeCrop(src: Bitmap, r: android.graphics.RectF): Bitmap? {
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

    private fun normalizePlate(raw: String?): String {
        val s0 = raw?.trim().orEmpty()
        if (s0.isBlank()) return ""
        var s = s0.uppercase(Locale.ROOT)
        s = s.replace('İ','I').replace('Ğ','G').replace('Ü','U')
            .replace('Ş','S').replace('Ö','O').replace('Ç','C')
        s = s.replace(" ", "").replace("-", "").replace("_", "")
        s = s.replace(Regex("[^A-Z0-9]"), "")
        if (s.length !in 5..16) return ""
        return s
    }

    private fun getNowIstanbulString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Europe/Istanbul")
        return sdf.format(Date())
    }

    private fun freezeUi(text: String) {
        uiFrozen = true
        runOnUiThread { tv.text = text }
    }

    private fun unfreezeUi() {
        uiFrozen = false
    }
}
