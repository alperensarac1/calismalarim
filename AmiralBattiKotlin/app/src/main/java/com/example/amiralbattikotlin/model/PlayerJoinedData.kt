package com.example.amiralbattikotlin.model


data class PlayerJoinedData(
    val roomCode: String,
    val players: List<PlayerInfo>,
    val message: String
)