package com.example.chatkotlin.service.response

data class SimpleResponse(
    val success: Boolean,
    val message: String? = null,
    val id: Int? = null,
    val error: String? = null
)
