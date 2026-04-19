package com.example.amiralbattikotlin.model

data class RoomCreatedData(
    val roomCode: String,
    val playerId: String,
    val players: List<PlayerInfo>,
    val message: String
)