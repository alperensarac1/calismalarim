package com.example.chatkotlin.model

data class Mesaj(
    val id: Int,
    val gonderen_id: Int,
    val alici_id: Int,
    val mesaj_text: String?,
    val resim_var: Int,
    val resim_url: String?,
    val tarih: String
)
