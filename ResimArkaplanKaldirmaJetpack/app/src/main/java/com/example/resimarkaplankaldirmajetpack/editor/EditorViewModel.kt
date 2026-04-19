package com.example.resimarkaplankaldirmajetpack.editor

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.ArrayDeque

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<Bitmap>()
    private val maxUndoCount = 10

    private var previewJob: Job? = null

    fun loadImage(uri: Uri) {
        viewModelScope.launch {
            updateState { it.copy(isProcessing = true, infoText = "Fotoğraf yükleniyor...") }

            val bitmap = withContext(Dispatchers.IO) {
                appContext.contentResolver.openInputStream(uri)?.use { input ->
                    val decoded = BitmapFactory.decodeStream(input)
                    decoded?.copy(Bitmap.Config.ARGB_8888, true)
                }
            }

            if (bitmap == null) {
                updateState {
                    it.copy(
                        isProcessing = false,
                        infoText = "Resim yüklenemedi."
                    )
                }
                return@launch
            }

            undoStack.clear()

            updateState {
                it.copy(
                    originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true),
                    workingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true),
                    isProcessing = false,
                    infoText = "Fotoğraf yüklendi. Silmek istediğiniz bölgeye dokunun.",
                    hasActivePreview = false,
                    previewBaseBitmap = null,
                    lastTappedX = -1,
                    lastTappedY = -1,
                    canUndo = false
                )
            }
        }
    }

    fun onToleranceChange(value: Float) {
        updateState { it.copy(tolerance = value) }

        val state = _uiState.value
        if (state.hasActivePreview) {
            renderPreviewFromActiveState()
        }
    }

    fun onImageTapped(bitmapX: Int, bitmapY: Int) {
        val state = _uiState.value
        val currentBitmap = state.workingBitmap ?: return

        if (bitmapX !in 0 until currentBitmap.width || bitmapY !in 0 until currentBitmap.height) {
            return
        }

        commitActivePreviewIfNeeded()

        val base = _uiState.value.workingBitmap ?: return
        saveStateForUndo(base)

        updateState {
            it.copy(
                previewBaseBitmap = base.copy(Bitmap.Config.ARGB_8888, true),
                lastTappedX = bitmapX,
                lastTappedY = bitmapY,
                hasActivePreview = true,
                infoText = "Canlı önizleme hazırlanıyor..."
            )
        }

        renderPreviewFromActiveState()
    }

    private fun renderPreviewFromActiveState() {
        previewJob?.cancel()

        val state = _uiState.value
        val baseBitmap = state.previewBaseBitmap ?: return
        val x = state.lastTappedX
        val y = state.lastTappedY
        val tolerance = state.tolerance

        if (x < 0 || y < 0) return

        previewJob = viewModelScope.launch {
            updateState { it.copy(isProcessing = true) }

            val resultBitmap = withContext(Dispatchers.Default) {
                val targetColor = baseBitmap.getPixel(x, y)

                ImageProcessor.removeConnectedRegionByColor(
                    source = baseBitmap,
                    startX = x,
                    startY = y,
                    targetColor = targetColor,
                    tolerance = tolerance
                )
            }

            updateState {
                it.copy(
                    workingBitmap = resultBitmap,
                    isProcessing = false,
                    infoText = "Canlı önizleme aktif. Tolerans: ${tolerance.toInt()}"
                )
            }
        }
    }

    private fun commitActivePreviewIfNeeded() {
        val state = _uiState.value
        if (!state.hasActivePreview) return

        updateState {
            it.copy(
                hasActivePreview = false,
                previewBaseBitmap = null,
                lastTappedX = -1,
                lastTappedY = -1
            )
        }
    }

    fun undo() {
        previewJob?.cancel()

        if (undoStack.isEmpty()) return

        val previousBitmap = undoStack.removeLast()

        updateState {
            it.copy(
                workingBitmap = previousBitmap,
                hasActivePreview = false,
                previewBaseBitmap = null,
                lastTappedX = -1,
                lastTappedY = -1,
                isProcessing = false,
                infoText = "Son işlem geri alındı.",
                canUndo = undoStack.isNotEmpty()
            )
        }
    }

    fun reset() {
        previewJob?.cancel()

        val original = _uiState.value.originalBitmap ?: return

        undoStack.clear()

        updateState {
            it.copy(
                workingBitmap = original.copy(Bitmap.Config.ARGB_8888, true),
                hasActivePreview = false,
                previewBaseBitmap = null,
                lastTappedX = -1,
                lastTappedY = -1,
                isProcessing = false,
                infoText = "Görsel sıfırlandı.",
                canUndo = false
            )
        }
    }

    fun saveImage() {
        commitActivePreviewIfNeeded()

        val bitmap = _uiState.value.workingBitmap ?: return

        viewModelScope.launch {
            updateState { it.copy(isProcessing = true, infoText = "Görsel kaydediliyor...") }

            val success = withContext(Dispatchers.IO) {
                saveBitmapAsPng(bitmap)
            }

            updateState {
                it.copy(
                    isProcessing = false,
                    infoText = if (success) {
                        "Arka plansız görsel PNG olarak kaydedildi."
                    } else {
                        "Kaydetme başarısız oldu."
                    }
                )
            }
        }
    }

    private fun saveStateForUndo(bitmap: Bitmap) {
        val snapshot = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        if (undoStack.size >= maxUndoCount) {
            undoStack.removeFirst()
        }

        undoStack.addLast(snapshot)
        updateState { it.copy(canUndo = true) }
    }

    private fun saveBitmapAsPng(bitmap: Bitmap): Boolean {
        return try {
            val fileName = "bg_removed_${System.currentTimeMillis()}.png"

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ColorRemover")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = appContext.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: return false

            var outputStream: OutputStream? = null
            try {
                outputStream = appContext.contentResolver.openOutputStream(uri) ?: return false
                val success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val updateValues = ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    }
                    appContext.contentResolver.update(uri, updateValues, null, null)
                }

                success
            } finally {
                outputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun updateState(block: (EditorUiState) -> EditorUiState) {
        _uiState.value = block(_uiState.value)
    }
}