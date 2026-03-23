package com.example.surusuygulamakotlin.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.surusuygulamakotlin.MainActivity
import com.example.surusuygulamakotlin.R
import com.example.surusuygulamakotlin.helper.LocationCache
import com.example.surusuygulamakotlin.helper.PlateAnalyzerHybrid
import com.example.surusuygulamakotlin.helper.PlateBuffer
import com.example.surusuygulamakotlin.helper.PlateDetectorHelper
import com.example.surusuygulamakotlin.helper.PlateStore
import com.example.surusuygulamakotlin.helper.UploadHelper
import com.example.surusuygulamakotlin.helper.VideoLoopRecorderBg
import com.example.surusuygulamakotlin.helper.VoskSendDetector
import com.example.surusuygulamakotlin.helper.YuvToRgbConverter
import com.example.surusuygulamakotlin.ui.MlKitOcr
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.atomic.AtomicBoolean

class RecordingForegroundService : LifecycleService() {

    private lateinit var loopRecorder: VideoLoopRecorderBg
    private var cameraProvider: ProcessCameraProvider? = null
    private var sendDetector: VoskSendDetector? = null

    private val analysisExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()
    private var yuvConverter: YuvToRgbConverter? = null
    private var detector: PlateDetectorHelper? = null
    private var ocr: MlKitOcr? = null

    private val TAG_SEND = "SEND_FLOW"
    private val KEY_LAST_PLATES_JSON = "last_plates_json"

    private val started = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannelIfNeeded()

        loopRecorder = VideoLoopRecorderBg(
            context = this,
            lifecycleOwner = this,
            segmentMs = 30_000L,
            keepSegments = 2
        )

        sendDetector = VoskSendDetector(this) { onSendDetected() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG_SEND, "onStartCommand ✅ service started")

        try {
            startForeground(NOTI_ID, buildNotification("Hazırlanıyor..."))
        } catch (t: Throwable) {
            Log.e("REC_SVC", "startForeground FAILED", t)
            stopSelf()
            return START_NOT_STICKY
        }

        super.onStartCommand(intent, flags, startId)
        setRunning(true)

        if (started.compareAndSet(false, true)) {
            try {
                startCameraAndLoop()
            } catch (t: Throwable) {
                val msg = "Kamera başlatma hatası: ${t.message}"
                updateNotification(msg)
                sendUiEvent(msg, isError = true)
            }

            sendDetector?.start(
                onStatus = { s ->
                    updateNotification(s)
                    sendUiEvent(s, isError = false)
                },
                onError = { err ->
                    val msg = "Vosk hata: ${err.message}"
                    updateNotification(msg)
                    sendUiEvent(msg, isError = true)
                }
            )
        } else {
            Log.e(TAG_SEND, "already started -> skipping startCameraAndLoop()")
            updateNotification("Kayıt devam ediyor...")
        }

        return START_STICKY
    }

    override fun onDestroy() {
        try { stopForeground(true) } catch (_: Throwable) {}
        try { loopRecorder.stopLoop() } catch (_: Throwable) {}
        try { cameraProvider?.unbindAll() } catch (_: Throwable) {}

        try { sendDetector?.stop() } catch (_: Throwable) {}
        sendDetector = null

        try { analysisExecutor.shutdown() } catch (_: Throwable) {}
        try { yuvConverter?.release() } catch (_: Throwable) {}
        yuvConverter = null

        try { detector?.close() } catch (_: Throwable) {}
        detector = null
        ocr = null

        setRunning(false)
        started.set(false)

        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        try { stopForeground(true) } catch (_: Throwable) {}
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(NOTI_ID)
        } catch (_: Throwable) {}

        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    private fun startCameraAndLoop() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            cameraProvider = future.get()
            val provider = cameraProvider ?: return@addListener

            // ✅ RGBA format (S20 FE fix)
            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888) // ✅ ekle
                .build()


            // Converter'ı yine tutuyoruz (fallback için)
            val converter = yuvConverter ?: YuvToRgbConverter(this).also { yuvConverter = it }
            val det = detector ?: PlateDetectorHelper(this).also { detector = it }
            val o = ocr ?: MlKitOcr().also { ocr = it }

            analysis.setAnalyzer(
                analysisExecutor,
                PlateAnalyzerHybrid(
                    converter = converter, // RGBA’da kullanılmayacak, fallback hazır
                    detector = det,
                    ocr = o,
                    onDebug = { msg -> Log.d("PlateHybrid", msg) },
                    onOverlay = { _, _, _ -> }
                )
            )

            loopRecorder.bindCamera(provider, analysis)

            loopRecorder.startLoop(
                onStatus = {
                    updateNotification(it)
                    sendUiEvent(it, isError = false)
                },
                onSegmentSaved = { uri ->
                    saveLastVideoUri(uri)

                    lifecycleScope.launch {
                        val plates = collectPlatesForUpload()
                        saveLastPlatesJson(plates)

                        val msg = "Segment OK: $uri (plates=${plates.size})"
                        updateNotification(msg)
                        sendUiEvent(msg, isError = false)

                        try {
                            LocationCache.getMandatory(this@RecordingForegroundService, ::getNowIstanbulString)
                        } catch (_: Throwable) {}
                    }
                },
                onError = { err ->
                    val msg = "Kayıt hatası: ${err.message}"
                    updateNotification(msg)
                    sendUiEvent(msg, isError = true)
                }
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun onSendDetected() {
        Log.e(TAG_SEND, "SEND detected ✅ -> cutting segment...")
        updateNotification("“gönder” algılandı → segment kesiliyor...")

        loopRecorder.cutNow { uri ->
            if (uri == null) {
                updateNotification("Segment kesilemedi")
                sendUiEvent("Segment kesilemedi", isError = true)
                return@cutNow
            }

            saveLastVideoUri(uri)

            lifecycleScope.launch {
                val plates = collectPlatesForUpload()
                saveLastPlatesJson(plates)

                val clientSentAt = getNowIstanbulString()
                uploadNow(uri, plates, clientSentAt)
            }
        }
    }

    private suspend fun collectPlatesForUpload(): List<String> {
        var list = PlateBuffer.snapshotLast30s()
        if (list.isNotEmpty()) return list

        delay(1100L)

        list = PlateBuffer.snapshotLast30s()
        if (list.isNotEmpty()) return list

        val last = PlateStore.getIfFresh(windowMs = 30_000L)
        if (!last.isNullOrBlank()) return listOf(last)

        return listOf("UNKNOWN")
    }

    private fun uploadNow(videoUri: Uri, plates: List<String>, clientSentAtStr: String) {
        val startMsg = "Upload hazırlanıyor..."
        updateNotification(startMsg)
        sendUiEvent(startMsg, isError = false)

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val loc = LocationCache.getMandatory(
                        ctx = this@RecordingForegroundService,
                        nowStr = ::getNowIstanbulString,
                        freshMaxAgeMs = 60_000L
                    )

                    val tempFile = UploadHelper.copyUriToTempFile(this@RecordingForegroundService, videoUri)
                    val videoPart = UploadHelper.buildVideoPart(tempFile)

                    val safePlates = if (plates.isNotEmpty()) plates else listOf("UNKNOWN")
                    val platesJsonStr = Gson().toJson(safePlates)

                    val textType = "text/plain".toMediaType()

                    Log.e(TAG_SEND, "plates_json=$platesJsonStr")
                    Log.e(TAG_SEND, "loc=${loc.lat},${loc.lng} acc=${loc.acc} at=${loc.at}")

                    val resp = ApiClient.api.uploadReport(
                        video = videoPart,
                        platesJson = platesJsonStr.toRequestBody(textType),
                        clientSentAt = clientSentAtStr.toRequestBody(textType),
                        deviceLat = loc.lat.toString().toRequestBody(textType),
                        deviceLng = loc.lng.toString().toRequestBody(textType),
                        deviceAcc = loc.acc.toString().toRequestBody(textType),
                        deviceLocAt = loc.at.toRequestBody(textType)
                    )

                    try { tempFile.delete() } catch (_: Throwable) {}
                    resp
                }

                if (result.ok) {
                    val url = result.data?.video_url ?: "-"
                    val okMsg = "Upload OK ✅ url: $url"
                    updateNotification(okMsg)
                    sendUiEvent(okMsg, isError = false)
                } else {
                    val failMsg = "Upload FAIL ❌ ${result.message}"
                    updateNotification(failMsg)
                    sendUiEvent(failMsg, isError = true)
                }
            } catch (e: Throwable) {
                val errMsg = "Upload iptal ❌ ${e.message}"
                updateNotification(errMsg)
                sendUiEvent(errMsg, isError = true)
            }
        }
    }

    private fun saveLastVideoUri(uri: Uri) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_URI, uri.toString()).apply()
    }

    private fun saveLastPlatesJson(plates: List<String>) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val json = Gson().toJson(if (plates.isEmpty()) listOf("UNKNOWN") else plates)
        prefs.edit().putString(KEY_LAST_PLATES_JSON, json).apply()
        Log.e("PLATE_SAVE", "last_plates_json=$json")
    }

    private fun setRunning(value: Boolean) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_RUNNING, value).apply()
    }

    private fun sendUiEvent(message: String, isError: Boolean) {
        val i = Intent(UiEvents.ACTION_SERVICE_EVENT).apply {
            setPackage(packageName)
            putExtra(UiEvents.EXTRA_MSG, message)
            putExtra(UiEvents.EXTRA_IS_ERROR, isError)
        }
        sendBroadcast(i)
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTI_ID, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Kayıt aktif (30sn döngü)")
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(pending)
            .build()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Kayıt Servisi", NotificationManager.IMPORTANCE_LOW)
        )
    }

    private fun getNowIstanbulString(): String {
        val tz = java.util.TimeZone.getTimeZone("Europe/Istanbul")
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        sdf.timeZone = tz
        return sdf.format(java.util.Date())
    }

    companion object {
        private const val CHANNEL_ID = "rec_channel"
        private const val NOTI_ID = 2001

        private const val PREFS = "rec_prefs"
        private const val KEY_LAST_URI = "last_video_uri"
        private const val KEY_RUNNING = "service_running"

        private const val TAG = "SERVICE_START"

        fun start(context: Context) {
            try {
                Log.e(TAG, "start() called ✅")
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, RecordingForegroundService::class.java)
                )
            } catch (t: Throwable) {
                Log.e(TAG, "start() FAILED ❌ ${t.message}")
            }
        }

        fun stop(context: Context) {
            try {
                Log.e(TAG, "stop() called ✅")
                context.stopService(Intent(context, RecordingForegroundService::class.java))
            } catch (t: Throwable) {
                Log.e(TAG, "stop() FAILED ❌ ${t.message}")
            }
        }
    }
}
