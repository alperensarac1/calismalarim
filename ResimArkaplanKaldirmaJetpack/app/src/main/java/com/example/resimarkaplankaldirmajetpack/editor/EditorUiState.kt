package com.example.resimarkaplankaldirmajetpack.editor

import android.graphics.Bitmap

data class EditorUiState(
    val originalBitmap: Bitmap? = null,
    val workingBitmap: Bitmap? = null,

    val tolerance: Float = 60f,
    val infoText: String = "Önce fotoğraf seçin. Sonra silmek istediğiniz bölgeye dokunun.",

    val isProcessing: Boolean = false,

    val hasActivePreview: Boolean = false,
    val previewBaseBitmap: Bitmap? = null,
    val lastTappedX: Int = -1,
    val lastTappedY: Int = -1,

    val canUndo: Boolean = false
)