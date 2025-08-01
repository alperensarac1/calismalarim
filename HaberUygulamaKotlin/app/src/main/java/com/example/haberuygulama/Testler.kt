package com.example.haberuygulama

import com.example.haberuygulama.deo.HaberDao
import com.example.haberuygulama.model.HaberModel
import com.example.haberuygulama.servis.ApiClient
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val haberDao = HaberDao(ApiClient.retrofit)

    println("🔄 Son dakika haberler yükleniyor...")
    val sonDakika: List<HaberModel>? = haberDao.getSonDakikaHaberler()
    if (sonDakika != null) {
        println("✅ SonDakika Haber Sayısı: ${sonDakika.size}")
        sonDakika.forEach {
            println("• ${it.baslik}")
        }
    } else {
        println("❌ Hata: SonDakika haberleri alınamadı")
    }

    println("\n🔄 Gündem haberler yükleniyor...")
    val gundem = haberDao.getGundemHaberler()
    if (gundem != null) {
        println("✅ Gündem Haber Sayısı: ${gundem.size}")
        gundem.forEach {
            println("• ${it.baslik}")
        }
    } else {
        println("❌ Hata: Gündem haberleri alınamadı")
    }

    println("\n🔄 Kategoriler yükleniyor...")
    val kategoriler = haberDao.getKategoriler()
    if (kategoriler != null) {
        println("✅ Kategori Sayısı: ${kategoriler.size}")
        kategoriler.forEach {
            println("• ${it.tur_adi}")
        }
    } else {
        println("❌ Hata: Kategoriler alınamadı")
    }
}
