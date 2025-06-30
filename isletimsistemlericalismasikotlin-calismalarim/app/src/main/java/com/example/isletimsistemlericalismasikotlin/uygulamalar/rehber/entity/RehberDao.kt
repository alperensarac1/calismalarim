package com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.entity

import android.content.ContentValues
import com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.model.RehberKisiler

class RehberDao(private val vt: RehberVeritabaniYardimcisi) {

    fun kisiEkle(kisiIsim: String, kisiNumara: String) {
        val dbx = vt.writableDatabase
        val degerler = ContentValues().apply {
            put("kisi_isim", kisiIsim)
            put("kisi_numara", kisiNumara)
        }
        dbx.insertOrThrow("rehber", null, degerler)
        dbx.close()
    }

    fun tumKisiler(): ArrayList<RehberKisiler> {
        val tumKisiler = ArrayList<RehberKisiler>()
        val dbx = vt.writableDatabase
        val cursor = dbx.rawQuery("SELECT * FROM rehber", null)

        while (cursor.moveToNext()) {
            val kisi = RehberKisiler(
                cursor.getInt(cursor.getColumnIndexOrThrow("kisi_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("kisi_isim")),
                cursor.getString(cursor.getColumnIndexOrThrow("kisi_numara"))
            )
            tumKisiler.add(kisi)
        }
        cursor.close()
        dbx.close()
        return tumKisiler
    }

    fun kisiSil(kisiId: Int) {
        val dbx = vt.writableDatabase
        dbx.delete("rehber", "kisi_id=?", arrayOf(kisiId.toString()))
        dbx.close()
    }

    fun kisiGuncelle(kisiId: Int, kisiIsim: String, kisiNumara: String) {
        val dbx = vt.writableDatabase
        val degerler = ContentValues().apply {
            put("kisi_id", kisiId)
            put("kisi_isim", kisiIsim)
            put("kisi_numara", kisiNumara)
        }
        dbx.update("rehber", degerler, "kisi_id=?", arrayOf(kisiId.toString()))
        dbx.close()
    }
}
