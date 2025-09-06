package com.example.memesharejetpack.model

data class ImageUploadRequest(
    val room_id: Int,
    val user_id: Int,
    val base64_image: String,
    val caption: String
)