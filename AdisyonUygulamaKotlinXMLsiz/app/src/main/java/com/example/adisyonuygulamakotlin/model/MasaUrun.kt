package com.example.adisyonuygulamakotlin.model

data class MasaUrun(
    val urun_id: Int,
    val urun_ad: String,
    val birim_fiyat: Float,
    val adet: Int,
    val toplam_fiyat: Float
)
