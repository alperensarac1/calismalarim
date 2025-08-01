package com.example.haberuygulamajetpack.navigation


import android.net.Uri
import com.example.haberuygulamajetpack.model.HaberModel
import com.google.gson.Gson

sealed class Screen(val route: String) {
    object Anasayfa : Screen("anasayfa")

    // Detay ekranı
    object Detay : Screen("detay/{haberJson}") {
        fun withArgs(haber: HaberModel): String {
            val haberJson = Gson().toJson(haber)  // `HaberModel`'i JSON'a dönüştürüyoruz
            return "detay/${Uri.encode(haberJson)}"
        }
    }

    // Kategori ekranı
    object Kategori : Screen("kategori/{kategoriId}") {
        fun withArgs(kategoriId: Int): String {
            return "kategori/$kategoriId"
        }
    }
}
