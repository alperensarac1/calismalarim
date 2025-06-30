package com.example.qrkodolusturucu.services

import com.example.qrkodolusturucu.model.CRUDCevap
import com.example.qrkodolusturucu.model.KodUretCevap
import com.example.qrkodolusturucu.model.UrunCevap
import com.example.qrkodolusturucu.model.UrunKategori

interface Services {
    suspend fun kullaniciEkle(kisiTel:String): CRUDCevap
    suspend fun tumKahveler(): UrunCevap
    suspend fun kodUret(dogrulamaKodu:String,kisiTel:String): KodUretCevap
    suspend fun kahveWithKategoriId(urunKategori: UrunKategori): UrunCevap
    suspend fun kodSil(dogrulamaKodu: String): KodUretCevap
}