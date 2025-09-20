package com.example.cookieclickerjetpack.model

data class FloatingText(
    val id: Long,
    val text: String,
    val x: Float,
    val y: Float,
    val isCrit: Boolean = false
)