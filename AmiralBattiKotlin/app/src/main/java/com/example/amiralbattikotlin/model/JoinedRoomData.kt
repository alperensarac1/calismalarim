package com.example.amiralbattikotlin.model

data class JoinedRoomData(
    val roomCode: String,
    val playerId: String,
    val players: List<PlayerInfo>,
    val message: String
)