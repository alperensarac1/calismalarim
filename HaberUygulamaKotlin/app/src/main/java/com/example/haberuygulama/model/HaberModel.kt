package com.example.haberuygulama.model

import java.io.Serializable

data class HaberModel(
    val id: Int,
    val baslik: String,
    val icerik: String,
    val media_type: String,
    val media_url: String,
    val yayinlanma_tarihi: String,
    val sondakika: Int,
    val yazar_id: Int?,
    val tur_id: Int?,
    val ad: String?,           // yazar adı
    val soyad: String?,        // yazar soyadı
    val unvan: String?,        // yazar unvanı
    val tur_adi: String?       // tür adı
):Serializable

