package com.example.isletimsistemlericalismasikotlin.giris_ekrani.repository

import android.content.Context
import android.content.SharedPreferences

class KullaniciBilgileri(val context: Context) {

    lateinit var sp: SharedPreferences
    lateinit var editor :SharedPreferences.Editor

    init {
        sp = context.getSharedPreferences("KullaniciBilgileri",Context.MODE_PRIVATE)
        editor = sp.edit()
    }

    fun sifreKaydiOlusturulmusMu():Boolean{
        return sp.getBoolean("kayitolusturulmusmu",false)
    }
    fun sifreKaydi(sifre:String){
        editor.putString("sifre",sifre)
        editor.putBoolean("kayitolusturulmusmu",true)
        onayla()
    }

    fun sifreSorgulama(dogrulanacakSifre:String):Boolean{
        return dogrulanacakSifre.equals(sp.getString("sifre",""))
    }
    fun guvenlikSorusuKaydet(guvenlikSorusu:String){
        editor.putString("guvenliksorusu",guvenlikSorusu)
        onayla()
    }
    fun guvenlikSorusuGetir():String{
        return sp.getString("guvenliksorusu","")!!
    }
    fun guvenlikSorusuCevapKaydet(guvenlikSorusuCevap:String){
        editor.putString("guvenliksorusucevap",guvenlikSorusuCevap)
        onayla()
    }
    fun guvenlikSorusuDogrula(dogrulanacakCevap:String):Boolean{
        return dogrulanacakCevap.equals(sp.getString("guvenliksorusucevap",dogrulanacakCevap))
    }
    fun sifreSorulsunMuDegistir(sifreSorulsunMu:Boolean){
        editor.putBoolean("sifresorulsunmu",sifreSorulsunMu)
        onayla()
    }
    fun sifreSorulsunMu():Boolean{
        return sp.getBoolean("sifresorulsunmu",true)
    }
    fun sifreDegistir(degistirilecekSifre:String){
        editor.putString("sifre",degistirilecekSifre)
        onayla()
    }
    fun kullaniciBilgileriSifirla(){
        editor.remove("kayitolusturulmusmu");
        editor.remove("sifre");
        editor.remove("guvenliksorusu");
        editor.remove("sifresorulsunmu");
        onayla();
    }

    private fun onayla(){
        editor.apply()
    }
}
