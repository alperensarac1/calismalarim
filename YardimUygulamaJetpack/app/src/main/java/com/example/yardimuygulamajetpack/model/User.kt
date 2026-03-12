package com.example.yardimuygulamajetpack.model


data class User(
    val id: Long,
    val role: String,
    val ad: String? = null,
    val soyad: String? = null,
    val yas: Int? = null,
    val telefon: String? = null,
    val il: String? = null,
    val ilce: String? = null
)