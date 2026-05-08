package com.example.onlineradyokotlin.data

data class RadioRoom(
    val id: Int,
    val roomName: String,
    val currentMusic: String?,
    val isPlaying: Boolean,
    val listenerCount: Int
)