package com.example.qrkodolusturucujetpack.services.dummyservice

import com.example.qrkodolusturucu.services.Services

import com.example.qrkodolusturucu.model.CRUDCevap

import com.example.qrkodolusturucujetpack.model.KodUretCevap
import com.example.qrkodolusturucujetpack.model.UrunCevap
import com.example.qrkodolusturucujetpack.model.UrunKategori
import com.example.qrkodolusturucujetpack.services.dummyservice.Urunler.dummyUrunler
import java.util.UUID

class KahveDummyServisDao:Services {
    override suspend fun kullaniciEkle(kisiTel: String): CRUDCevap =
        CRUDCevap(success = 1, message = "Ekleme başarılı", kahveSayisi = 0, hediyeKahve = 0)

    override suspend fun tumKahveler(): UrunCevap =
        UrunCevap(dummyUrunler)

    override suspend fun kodUret(dogrulamaKodu: String, kisiTel: String): KodUretCevap =
        KodUretCevap(1,UUID.randomUUID().toString())

    override suspend fun kahveWithKategoriId(urunKategori: UrunKategori): UrunCevap =
        UrunCevap(dummyUrunler.filter { it.urunKategoriId == urunKategori.kategoriKodu() })

    override suspend fun kodSil(dogrulamaKodu: String): KodUretCevap = KodUretCevap(1,"Kod başarıyla silindi")

}