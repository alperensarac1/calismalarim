package com.example.haberuygulamajetpack.model

import java.io.Serializable

data class HaberModel(
    val id: Int,
    val baslik: String,
    val icerik: String,
    val media_type: String,
    val media_url: String,
    val yayinlanma_tarihi: String,
    val sondakika: Int,
    val yazar_id: Int,
    val tur_id: Int,
    val ad: String,
    val soyad: String,
    val unvan: String,
    val tur_adi: String
) : Serializable
