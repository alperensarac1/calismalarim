package com.example.adisyonuygulamakotlin.model

data class Urun(
    val id: Int,
    var urun_ad: String,
    var urun_fiyat: Float,
    var urun_resim: String,
    var urun_adet: Int,
    var urunKategori: Kategori
)
