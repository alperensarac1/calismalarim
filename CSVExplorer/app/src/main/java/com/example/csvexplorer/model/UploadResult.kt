package com.example.csvexplorer.model

data class UploadResult(
    val ok: Boolean,
    val downloadUrl: String?,
    val error: String?
)
