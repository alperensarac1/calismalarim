package com.example.dosyapaylasimjetpack.model

data class LinkResponse(
    val ok: Boolean,
    val code: String?,
    val original_name: String?,
    val size_bytes: Long?,
    val mime_type: String?,
    val created_at: String?,
    val expires_at: String?,
    val expired: Boolean?,
    val download_url: String?,
    val error: String?
)