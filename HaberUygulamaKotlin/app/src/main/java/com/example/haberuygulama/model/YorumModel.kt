package com.example.haberuygulama.model

data class YorumModel(
    val id: Int,
    val haber_id: Int,
    val takma_ad: String,
    val yorum_metni: String,
    val onayli: Int,
    val yorum_tarihi: String
)
