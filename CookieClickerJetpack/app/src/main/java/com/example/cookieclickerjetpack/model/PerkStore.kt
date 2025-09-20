package com.example.cookieclickerjetpack.model

data class PerkStore(
    val points: Int = 0,
    val gprod: Int = 0,     // %5/level global multiplier
    val crit: Int = 0,      // %1/level passive crit chance
    val discount: Int = 0,  // %2/level, cap %50
    val tapTop: Int = 0     // +1/level tap
)