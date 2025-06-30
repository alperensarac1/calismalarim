package com.example.qrkodolusturucujetpack.usecases

fun indirimYuzdeHesapla(indirimsizFiyat: Double, indirimliFiyat: Double): String {
    if (indirimsizFiyat <= 0) return "0.0"

    val indirimOrani = ((indirimsizFiyat - indirimliFiyat) / indirimsizFiyat) * 100
    return String.format("%.1f", indirimOrani)
}
