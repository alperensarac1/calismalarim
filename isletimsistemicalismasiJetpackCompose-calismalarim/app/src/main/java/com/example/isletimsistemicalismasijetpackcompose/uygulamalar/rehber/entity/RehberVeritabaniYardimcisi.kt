package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.entity

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class RehberVeritabaniYardimcisi(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE rehber (kisi_id INTEGER PRIMARY KEY AUTOINCREMENT, kisi_isim TEXT, kisi_numara TEXT);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS rehber") // Hatalı tablo adı "kelimeler" yerine "rehber" olarak düzeltildi.
        onCreate(db)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "rehberveritabani"
    }
}
