package com.example.haberuygulama.model

data class HaberTuruModel(
    val id: Int,
    val tur_adi: String
){
    override fun toString(): String = tur_adi
}
