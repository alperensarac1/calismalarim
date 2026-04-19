package com.example.amiralbattijetpack.model

data class BoardSetData(
    val roomCode: String,
    val players: List<PlayerInfo>,
    val message: String
)
