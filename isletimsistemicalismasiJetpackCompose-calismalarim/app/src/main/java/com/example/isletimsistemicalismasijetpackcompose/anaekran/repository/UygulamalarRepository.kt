package com.example.isletimsistemicalismasijetpackcompose.anaekran.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.isletimsistemicalismasijetpackcompose.anaekran.model.UygulamalarModel

class UygulamalarRepository(private val context: Context) {

    private val sp: SharedPreferences = context.getSharedPreferences("copKutusu", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sp.edit()

    fun copKutusunaTasi(uygulama: UygulamalarModel) {
        editor.putBoolean(uygulama.uygulamaAdi.replace(" ", "") + "Cop", true)
        editor.apply()
    }

    fun copKutusundanCikar(uygulama: UygulamalarModel) {
        editor.putBoolean(uygulama.uygulamaAdi.replace(" ", "") + "Cop", false)
        editor.apply()
    }
}
