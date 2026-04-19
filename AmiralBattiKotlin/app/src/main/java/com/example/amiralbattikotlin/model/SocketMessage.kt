package com.example.amiralbattikotlin.model

data class SocketMessage(
    val type: String,
    val data: Map<String, Any?>? = null
)