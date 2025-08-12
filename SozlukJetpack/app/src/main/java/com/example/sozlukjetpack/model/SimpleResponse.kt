package com.example.sozlukjetpack.model

data class SimpleResponse(
    val success: Boolean,
    val message: String? = null,
    val user_id: Int? = null,
    val entry_id: Int? = null,
    val comment_id: Int? = null
)
