package com.example.memesharekotlinn.model

data class UploadResponse(
    val success: Boolean,
    val message: String,
    val media_url: String
)