package com.example.haberuygulama.model

data class YorumInsertRequest(
    val haber_id: Int,
    val takma_ad: String,
    val yorum_metni: String
)
