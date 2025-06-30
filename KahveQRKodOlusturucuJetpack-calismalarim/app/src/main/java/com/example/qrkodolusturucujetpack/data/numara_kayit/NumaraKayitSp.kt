package com.example.qrkodolusturucujetpack.data.numara_kayit

import android.content.Context
import android.content.Context.MODE_PRIVATE
import javax.inject.Inject


class NumaraKayitSp @Inject constructor(context: Context) {
    val sp = context.getSharedPreferences("kullanici_bilgileri",MODE_PRIVATE)


    val editor = sp.edit()

    fun numaraKeydedildi(){
        editor.putBoolean("kullaniciNumarasiKayitliMi",true).apply()
    }
    fun numaraKaydedildiMi():Boolean{
        return sp.getBoolean("kullaniciNumarasiKayitliMi",false)
    }
    fun numaraGetir():String{
        return sp.getString("numara","")!!
    }
    fun numaraKaydet(kullaniciNumarasi:String){
        editor.putString("numara", kullaniciNumarasi).apply()
        numaraKeydedildi()
    }

}