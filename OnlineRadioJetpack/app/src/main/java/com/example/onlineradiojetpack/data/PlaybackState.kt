package com.example.onlineradiojetpack.data


data class PlaybackState(
    val roomId: Int,
    val title: String,
    val musicUrl: String,
    val positionSeconds: Double,
    val isPlaying: Boolean
)