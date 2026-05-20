package com.example.canliyayinjetpack.model

data class ChatMessageModel(
    val roomId: String,
    val username: String,
    val message: String,
    val createdAt: String
)