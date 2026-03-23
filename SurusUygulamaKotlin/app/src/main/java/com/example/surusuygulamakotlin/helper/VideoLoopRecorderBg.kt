package com.example.surusuygulamakotlin.helper

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean

class VideoLoopRecorderBg(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val segmentMs: Long = 30_000L,
    private val keepSegments: Int = 2
) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    private val looping = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var lastCompletedUri: Uri? = null

    fun getLastCompletedUri(): Uri? = lastCompletedUri

    // cut support
    @Volatile private var pendingCutCallback: ((Uri?) -> Unit)? = null
    @Volatile private var cutRequested = false

    // segment timer safety
    @Volatile private var segmentToken: Long = 0L

    private val recentUris: ArrayDeque<Uri> = ArrayDeque()

    fun bindCamera(
        cameraProvider: ProcessCameraProvider,
        analysisUseCase: ImageAnalysis,
        previewUseCase: Preview? = null
    ) {
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        cameraProvider.unbindAll()

        if (previewUseCase != null) {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                previewUseCase,
                videoCapture,
                analysisUseCase
            )
        } else {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                videoCapture,
                analysisUseCase
            )
        }
    }

    fun startLoop(
        onStatus: (String) -> Unit,
        onSegmentSaved: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (looping.getAndSet(true)) return

        val hasMic = hasRecordAudioPermission()
        onStatus(if (hasMic) "Loop kayıt (video+ses)" else "Loop kayıt (sadece video)")

        startOneSegment(onStatus, onSegmentSaved, onError)
    }

    fun stopLoop() {
        looping.set(false)
        segmentToken++ // timer’ları etkisizleştir
        stopRecordingSafely()
    }

    fun isLooping(): Boolean = looping.get()
    fun getRecentSegments(): List<Uri> = recentUris.toList()
    fun getLastSegment(): Uri? = recentUris.lastOrNull()

    fun cutNow(onCutFinalized: (Uri?) -> Unit) {
        val ar = activeRecording
        if (ar == null) {
            onCutFinalized(null)
            return
        }

        pendingCutCallback = onCutFinalized
        cutRequested = true

        // Eski timer'ın yeni segmente bulaşmasını engelle
        segmentToken++

        stopRecordingSafely()
    }

    private fun startOneSegment(
        onStatus: (String) -> Unit,
        onSegmentSaved: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (!looping.get()) return

        val vc = videoCapture ?: run {
            onError(IllegalStateException("VideoCapture null"))
            return
        }

        val name = "seg_${System.currentTimeMillis()}.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/SendLoop")
        }

        val outputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        onStatus("Segment başladı (30sn)...")

        // Bu segment için token üret
        val myToken = ++segmentToken

        try {
            val pending = vc.output.prepareRecording(context, outputOptions)
            val starter = if (hasRecordAudioPermission()) pending.withAudioEnabled() else pending

            activeRecording = starter.start(ContextCompat.getMainExecutor(context)) { event ->
                if (event is VideoRecordEvent.Finalize) {
                    activeRecording = null

                    if (event.hasError()) {
                        onError(RuntimeException("Record error: ${event.error}"))

                        if (cutRequested) {
                            cutRequested = false
                            pendingCutCallback?.invoke(null)
                            pendingCutCallback = null
                        }

                        if (looping.get()) {
                            mainHandler.postDelayed({
                                startOneSegment(onStatus, onSegmentSaved, onError)
                            }, 800)
                        }
                        return@start
                    }

                    val uri = event.outputResults.outputUri
                    lastCompletedUri = uri
                    pushRecent(uri)

                    onSegmentSaved(uri)

                    if (cutRequested) {
                        cutRequested = false
                        pendingCutCallback?.invoke(uri)
                        pendingCutCallback = null
                    }

                    if (looping.get()) {
                        mainHandler.post {
                            startOneSegment(onStatus, onSegmentSaved, onError)
                        }
                    }
                }
            }
        } catch (se: SecurityException) {
            looping.set(false)

            if (cutRequested) {
                cutRequested = false
                pendingCutCallback?.invoke(null)
                pendingCutCallback = null
            }

            onError(se)
            return
        }

        // 30sn sonra sadece “bu segment” ise durdur
        mainHandler.postDelayed({
            if (segmentToken == myToken) {
                stopRecordingSafely()
            }
        }, segmentMs)
    }

    private fun stopRecordingSafely() {
        try { activeRecording?.stop() } catch (_: Throwable) {}
    }

    private fun pushRecent(uri: Uri) {
        recentUris.addLast(uri)

        while (recentUris.size > keepSegments) {
            val old = recentUris.removeFirst()
            deleteUriSafely(old)
        }
    }

    private fun deleteUriSafely(uri: Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (_: Throwable) {}
    }

    private fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}
