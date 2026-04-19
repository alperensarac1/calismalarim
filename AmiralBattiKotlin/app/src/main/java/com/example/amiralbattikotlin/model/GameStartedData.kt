package com.example.amiralbattikotlin.model

data class GameStartedData(
    val roomCode: String,
    val firstTurnPlayerId: String,
    val players: List<PlayerInfo>,
    val message: String
)