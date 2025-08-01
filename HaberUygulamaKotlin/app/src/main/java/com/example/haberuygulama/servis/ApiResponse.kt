package com.example.haberuygulama.servis

data class ApiResponse(
    val success: Boolean,
    val id: Int? = null,
    val error: String? = null
)
