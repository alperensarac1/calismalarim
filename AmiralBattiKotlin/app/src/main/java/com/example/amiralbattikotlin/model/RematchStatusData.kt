package com.example.amiralbattikotlin.model

data class RematchStatusData(
    val roomCode: String,
    val players: List<RematchPlayerInfo>,
    val message: String
)