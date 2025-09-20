package com.example.cookieclickerkotlin


data class PrestigePerk(
    val key: String,
    val title: String,
    val desc: String,
    val baseCost: Int = 1,
    val costScaling: Double = 1.5,
    var level: Int = 0,
    val maxLevel: Int = Int.MAX_VALUE
) {
    fun costForNext(): Int {
        return (baseCost * Math.pow(costScaling, level.toDouble())).toInt().coerceAtLeast(baseCost)
    }
}

