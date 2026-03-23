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
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean


class VideoLoopRecorder(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val segmentMs: Long = 30_000L
) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    private val looping = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val recentUris: ArrayDeque<Uri> = ArrayDeque()
    private val maxRecent = 6

    fun bindCamera(cameraProvider: ProcessCameraProvider, previewView: PreviewView) {
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            videoCapture
        )
    }

    fun startLoop(
        onStatus: (String) -> Unit,
        onSegmentSaved: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (looping.getAndSet(true)) return

        val hasMic = hasRecordAudioPermission()
        onStatus(if (hasMic) "Loop kayıt başladı (video+ses)" else "Loop kayıt başladı (sadece video)")

        startOneSegment(onStatus, onSegmentSaved, onError)
    }

    fun stopLoop() {
        looping.set(false)
        stopRecordingSafely()
    }

    fun isLooping(): Boolean = looping.get()

    fun getRecentSegments(): List<Uri> = recentUris.toList()

    fun getLastSegment(): Uri? = recentUris.lastOrNull()

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

        onStatus("Segment kayıt (30sn) başladı...")

        try {
            val pending = vc.output.prepareRecording(context, outputOptions)

            val starter = if (hasRecordAudioPermission()) {
                pending.withAudioEnabled()
            } else {
                pending
            }

            activeRecording = starter.start(ContextCompat.getMainExecutor(context)) { event ->
                if (event is VideoRecordEvent.Finalize) {
                    activeRecording = null

                    if (event.hasError()) {
                        onError(RuntimeException("Record error: ${event.error}"))
                        if (looping.get()) {
                            mainHandler.postDelayed({
                                startOneSegment(onStatus, onSegmentSaved, onError)
                            }, 800)
                        }
                    } else {
                        val uri = event.outputResults.outputUri
                        pushRecent(uri)
                        onSegmentSaved(uri)

                        if (looping.get()) {
                            mainHandler.post {
                                startOneSegment(onStatus, onSegmentSaved, onError)
                            }
                        }
                    }
                }
            }
        } catch (se: SecurityException) {
            looping.set(false)
            onError(se)
            return
        }

        mainHandler.postDelayed({
            stopRecordingSafely()
        }, segmentMs)
    }

    private fun stopRecordingSafely() {
        try { activeRecording?.stop() } catch (_: Throwable) {}
    }

    private fun pushRecent(uri: Uri) {
        recentUris.addLast(uri)
        while (recentUris.size > maxRecent) recentUris.removeFirst()
    }

    private fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}
