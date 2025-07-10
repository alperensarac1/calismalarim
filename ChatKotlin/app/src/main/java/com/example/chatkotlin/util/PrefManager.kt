package com.example.chatkotlin.util

import android.content.Context
import android.content.SharedPreferences

class PrefManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mesajlasma_pref", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_KULLANICI_ID = "kullanici_id"
    }

    fun kaydetKullaniciId(id: Int) {
        prefs.edit().putInt(KEY_KULLANICI_ID, id).apply()
    }

    fun getirKullaniciId(): Int {
        return prefs.getInt(KEY_KULLANICI_ID, -1)
    }

    fun temizleKullanici() {
        prefs.edit().remove(KEY_KULLANICI_ID).apply()
    }

    fun kullaniciVarMi(): Boolean {
        return getirKullaniciId() != -1
    }
}
