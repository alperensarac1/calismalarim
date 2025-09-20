package com.example.cookieclickerjetpack.model

data class Upgrade(
    val id: Int,
    val title: String,
    val desc: String,
    val icon: String,          // Material icon name, örn: "Bolt"
    val basePrice: Double,
    val cpsGain: Double = 0.0,
    val tapGain: Int = 0,
    val priceMultiplier: Double = 1.15,
    val level: Int = 0
) {
    fun currentPrice(): Double = basePrice * Math.pow(priceMultiplier, level.toDouble())
}