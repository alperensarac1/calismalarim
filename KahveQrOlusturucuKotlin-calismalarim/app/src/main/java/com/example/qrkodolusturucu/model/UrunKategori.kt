package com.example.qrkodolusturucu.model

enum class UrunKategori {
    INDIRIMLI, ATISTIRMALIKLAR, ICECEKLER;

    fun kategoriKodu(): Int {
        return when (this) {
            INDIRIMLI -> 0
            ATISTIRMALIKLAR -> 1
            ICECEKLER -> 2
        }
    }
}