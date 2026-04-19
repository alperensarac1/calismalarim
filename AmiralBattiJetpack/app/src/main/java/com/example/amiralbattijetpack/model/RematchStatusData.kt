package com.example.amiralbattijetpack.model

data class RematchStatusData(
    val roomCode: String,
    val players: List<RematchPlayerInfo>,
    val message: String
)
