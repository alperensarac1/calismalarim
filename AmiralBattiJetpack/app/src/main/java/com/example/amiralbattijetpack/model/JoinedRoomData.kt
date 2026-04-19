package com.example.amiralbattijetpack.model

data class JoinedRoomData(
    val roomCode: String,
    val playerId: String,
    val players: List<PlayerInfo>,
    val message: String
)
