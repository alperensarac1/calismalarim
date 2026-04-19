package com.example.amiralbattijetpack.model

data class FireResultData(
    val roomCode: String,
    val shooterPlayerId: String,
    val targetPlayerId: String,
    val row: Int,
    val col: Int,
    val hit: Boolean,
    val nextTurnPlayerId: String?,
    val gameOver: Boolean,
    val winnerPlayerId: String?,
    val message: String
)
