package com.example.memesharejetpack.model

data class UploadResponse(
    val success: Boolean,
    val message: String,
    val media_url: String
)