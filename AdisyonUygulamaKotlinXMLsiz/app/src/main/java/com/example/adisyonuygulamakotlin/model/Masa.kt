package com.example.adisyonuygulamakotlin.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Masa(
    val id: Int,
    val masa_adi: String,
    @SerializedName("acik_mi")
    val acikMi: Int,
    val sure: String,
    val toplam_fiyat: Float,

    @Transient
    var urunler: MutableList<Urun> = mutableListOf() // localde çalışır, Retrofit yoksayar
)

