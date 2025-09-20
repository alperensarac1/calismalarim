package com.example.cookieclickerjetpack.model

data class GameState(
    val score: Double = 0.0,
    val cps: Double = 0.0,
    val baseTap: Int = 1,
    val extraTap: Int = 0,
    val prestigeLevel: Int = 0
)