package com.example.qrkodolusturucu.services

import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.model.MasaUrun
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.services.response.KategoriSilResponse
import com.example.adisyonuygulamakotlin.services.response.UrunSilResponse
import okhttp3.ResponseBody


interface Services {
    suspend fun urunEkle(urunAd: String, fiyat: Float, kategoriId: Int, adet: Int, base64: String)
    suspend fun masaSil(masaId: Int):ResponseBody
    suspend fun masaEkle():ResponseBody
    suspend fun masalariGetir(): List<Masa>
    suspend fun urunleriGetir(): List<Urun>
    suspend fun masaUrunleriniGetir(masaId: Int): List<MasaUrun>
    suspend fun urunEkle(masaId: Int, urunId: Int, adet: Int): ResponseBody
    suspend fun masaOdemeYap(masaId: Int): ResponseBody
    suspend fun masaToplamFiyat(masaId: Int): Float
    suspend fun urunCikar(masaId: Int, urunId: Int): ResponseBody
    suspend fun kategorileriGetir(): ArrayList<Kategori>
    suspend fun masaGetir(masaId: Int): Masa
    suspend fun masaBirlestir(anaMasaId: Int,birlestirilecekMasaId: Int): ResponseBody
    suspend fun kategoriEkle(ad: String): ResponseBody
    suspend fun kategoriSil(id: Int): KategoriSilResponse
    suspend fun urunSil(urunAd: String): UrunSilResponse
}