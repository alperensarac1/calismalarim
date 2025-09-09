package com.example.dosyapaylasimjetpack.model

data class UploadResponse(
    val ok: Boolean,
    val code: String?,
    val download_url: String?,
    val info_url: String?,
    val expires_at: String?,
    val error: String?
)