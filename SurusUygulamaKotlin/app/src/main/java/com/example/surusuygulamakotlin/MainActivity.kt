package com.example.surusuygulamakotlin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.surusuygulamakotlin.helper.PlateAnalyzerHybrid
import com.example.surusuygulamakotlin.helper.PlateBuffer
import com.example.surusuygulamakotlin.helper.PlateDetectorHelper
import com.example.surusuygulamakotlin.helper.PlateStore
import com.example.surusuygulamakotlin.helper.UploadHelper
import com.example.surusuygulamakotlin.helper.VideoLoopRecorderBg
import com.example.surusuygulamakotlin.helper.YuvToRgbConverter
import com.example.surusuygulamakotlin.service.ApiClient
import com.example.surusuygulamakotlin.service.RecordingForegroundService
import com.example.surusuygulamakotlin.ui.MlKitOcr
import com.example.surusuygulamakotlin.ui.OverlayView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvLastVideo: TextView
    private lateinit var btnUpload: Button
    private lateinit var previewView: androidx.camera.view.PreviewView
    private lateinit var overlayView: OverlayView

    private val REQ_PERMS = 1001

    private lateinit var loopRecorder: VideoLoopRecorderBg
    private var cameraProvider: ProcessCameraProvider? = null

    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var yuvConverter: YuvToRgbConverter? = null
    private var detector: PlateDetectorHelper? = null
    private var ocr: MlKitOcr? = null

    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            updateLastVideoText()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvLastVideo = findViewById(R.id.tvLastVideo)
        btnUpload = findViewById(R.id.btnTestTrigger)
        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)

        loopRecorder = VideoLoopRecorderBg(
            context = this,
            lifecycleOwner = this,
            segmentMs = 30_000L,
            keepSegments = 2
        )

        btnUpload.setOnClickListener { uploadLastSegmentFromPrefs() }
        tvLastVideo.setOnClickListener { openLastVideo() }

        tvStatus.text = "Hazırlanıyor..."
        ensurePermissions()
    }

    override fun onStart() {
        super.onStart()
        RecordingForegroundService.stop(this)

        if (hasAllPermissions()) startCameraPreviewAndLoop()
        else tvStatus.text = "İzin gerekli (kamera + mikrofon + konum)"
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        handler.removeCallbacks(refreshRunnable)
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        stopCameraPreviewAndLoop()

        if (hasAllPermissions()) {
            RecordingForegroundService.start(this)
        }
    }

    override fun onDestroy() {
        stopCameraPreviewAndLoop()
        try { analysisExecutor.shutdown() } catch (_: Throwable) {}
        super.onDestroy()
    }

    private fun startCameraPreviewAndLoop() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            cameraProvider = future.get()
            val provider = cameraProvider ?: return@addListener

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888) // ✅ ekle
                .build()


            val converter = yuvConverter ?: YuvToRgbConverter(this).also { yuvConverter = it }
            val det = detector ?: PlateDetectorHelper(this).also { detector = it }
            val o = ocr ?: MlKitOcr().also { ocr = it }

            analysis.setAnalyzer(
                analysisExecutor,
                PlateAnalyzerHybrid(
                    converter = converter, // RGBA’da kullanılmayacak ama fallback hazır
                    detector = det,
                    ocr = o,
                    onDebug = { msg -> runOnUiThread { tvStatus.text = msg } },
                    onOverlay = { imgW, imgH, boxes ->
                        runOnUiThread {
                            overlayView.setResults(
                                imageWidth = imgW,
                                imageHeight = imgH,
                                viewWidth = previewView.width.coerceAtLeast(1),
                                viewHeight = previewView.height.coerceAtLeast(1),
                                boxes = boxes
                            )
                        }
                    }
                )
            )

            provider.unbindAll()
            loopRecorder.bindCamera(provider, analysisUseCase = analysis, previewUseCase = preview)

            loopRecorder.startLoop(
                onStatus = { s -> runOnUiThread { tvStatus.text = s } },
                onSegmentSaved = { uri ->
                    val prefs = getSharedPreferences("rec_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("last_video_uri", uri.toString())
                        .putString("last_full_video_uri", uri.toString())
                        .apply()

                    lifecycleScope.launch {
                        val plates = collectPlatesWithDelay()
                        val safePlates = if (plates.isEmpty()) listOf("UNKNOWN") else plates
                        val platesJson = Gson().toJson(safePlates)

                        prefs.edit()
                            .putString("last_plates_json", platesJson)
                            .apply()

                        runOnUiThread {
                            tvStatus.text = "Segment OK ✅ (plates=${safePlates.size})"
                        }
                    }
                },
                onError = { e ->
                    runOnUiThread { tvStatus.text = "Kayıt hatası: ${e.message}" }
                }
            )

        }, ContextCompat.getMainExecutor(this))
    }

    private suspend fun collectPlatesWithDelay(): List<String> {
        var list = PlateBuffer.snapshotLast30s()
        if (list.isNotEmpty()) return list

        delay(1100L)

        list = PlateBuffer.snapshotLast30s()
        if (list.isNotEmpty()) return list

        val last = PlateStore.getIfFresh(windowMs = 30_000L)
        if (!last.isNullOrBlank()) return listOf(last)

        return emptyList()
    }

    private fun stopCameraPreviewAndLoop() {
        try { loopRecorder.stopLoop() } catch (_: Throwable) {}
        try { cameraProvider?.unbindAll() } catch (_: Throwable) {}
        try { overlayView.setResults(1, 1, 1, 1, emptyList()) } catch (_: Throwable) {}
    }

    private fun buildRequiredPerms(): Array<String> {
        val list = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= 33) list.add(Manifest.permission.POST_NOTIFICATIONS)
        return list.toTypedArray()
    }

    private fun hasAllPermissions(): Boolean {
        return buildRequiredPerms().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun ensurePermissions() {
        if (hasAllPermissions()) return
        ActivityCompat.requestPermissions(this, buildRequiredPerms(), REQ_PERMS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQ_PERMS) return

        if (hasAllPermissions()) {
            Toast.makeText(this, "İzinler verildi ✅", Toast.LENGTH_SHORT).show()
            startCameraPreviewAndLoop()
        } else {
            tvStatus.text = "Konum izni ZORUNLU. Uygulama çalışmaz."
            Toast.makeText(this, "Konum izni olmadan devam edilemez", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun getLocationMandatory(): Triple<Double, Double, Float> {
        val fused = LocationServices.getFusedLocationProviderClient(this)
        val token = CancellationTokenSource()

        val loc = withContext(Dispatchers.IO) {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token).awaitCompat()
        } ?: throw IllegalStateException("Konum alınamadı")

        return Triple(loc.latitude, loc.longitude, loc.accuracy)
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitCompat(): T {
        return suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resumeWithException(it) }
            addOnCanceledListener { cont.cancel() }
        }
    }

    private fun uploadLastSegmentFromPrefs() {
        if (!hasAllPermissions()) {
            Toast.makeText(this, "İzinler eksik", Toast.LENGTH_LONG).show()
            return
        }

        val prefs = getSharedPreferences("rec_prefs", Context.MODE_PRIVATE)
        val lastFullUriStr = prefs.getString("last_full_video_uri", null)

        if (lastFullUriStr.isNullOrBlank()) {
            Toast.makeText(this, "İlk 30 saniye dolunca yeniden deneyin", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.parse(lastFullUriStr)
        tvStatus.text = "Upload hazırlanıyor..."

        lifecycleScope.launch {
            try {
                val platesNow = PlateBuffer.snapshotLast30s()
                val platesJson = if (platesNow.isNotEmpty()) {
                    Gson().toJson(platesNow)
                } else {
                    val last = PlateStore.getIfFresh(30_000L)
                    if (!last.isNullOrBlank()) Gson().toJson(listOf(last))
                    else {
                        val fromPrefs = prefs.getString("last_plates_json", null)
                        if (!fromPrefs.isNullOrBlank()) fromPrefs else Gson().toJson(listOf("UNKNOWN"))
                    }
                }

                val (lat, lng, acc) = getLocationMandatory()
                val nowStr = getNowIstanbulString()

                val resp = withContext(Dispatchers.IO) {
                    val tempFile = UploadHelper.copyUriToTempFile(this@MainActivity, uri)
                    val videoPart = UploadHelper.buildVideoPart(tempFile)
                    val textType = "text/plain".toMediaType()

                    val r = ApiClient.api.uploadReport(
                        video = videoPart,
                        platesJson = platesJson.toRequestBody(textType),
                        clientSentAt = nowStr.toRequestBody(textType),
                        deviceLat = lat.toString().toRequestBody(textType),
                        deviceLng = lng.toString().toRequestBody(textType),
                        deviceAcc = acc.toString().toRequestBody(textType),
                        deviceLocAt = nowStr.toRequestBody(textType)
                    )

                    try { tempFile.delete() } catch (_: Throwable) {}
                    r
                }

                tvStatus.text = if (resp.ok) "Upload OK ✅" else "Upload FAIL ❌ ${resp.message}"

            } catch (e: Throwable) {
                tvStatus.text = "Upload iptal ❌ ${e.message}"
            }
        }
    }

    private fun updateLastVideoText() {
        val prefs = getSharedPreferences("rec_prefs", Context.MODE_PRIVATE)
        val lastUri = prefs.getString("last_full_video_uri", null)
        tvLastVideo.text = if (lastUri.isNullOrBlank()) "Son video: -" else "Son video: $lastUri"
    }

    private fun openLastVideo() {
        val prefs = getSharedPreferences("rec_prefs", Context.MODE_PRIVATE)
        val lastUriStr = prefs.getString("last_full_video_uri", null) ?: return

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(lastUriStr), "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (_: Throwable) {
            Toast.makeText(this, "Video oynatıcı bulunamadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getNowIstanbulString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Europe/Istanbul")
        return sdf.format(Date())
    }
}
