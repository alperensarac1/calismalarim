package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.notdefteri

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NoteDatabase(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "NotlarDB"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createNotTable = """
            CREATE TABLE IF NOT EXISTS Notlar (
                id INTEGER PRIMARY KEY AUTOINCREMENT, 
                baslik TEXT, 
                notMetin TEXT, 
                listName TEXT, 
                imageUrl TEXT, 
                color TEXT, 
                tarih TEXT
            )
        """.trimIndent()
        db.execSQL(createNotTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Notlar")
        onCreate(db)
    }

    fun yeniNot(not: Not) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("baslik", not.baslik)
                put("notMetin", not.notMetin)
                put("listName", not.listName)
                put("imageUrl", not.imageUrl)
                put("color", not.renk)
                put("tarih", not.tarih)
            }
            db.insert("Notlar", null, values)
        }
    }


    @SuppressLint("Range")
    fun getNotlarim(): ArrayList<Not> {
        val notlar = ArrayList<Not>()
        readableDatabase.use { db ->
            val cursor = db.rawQuery("SELECT * FROM Notlar ORDER BY id DESC", null)
            cursor.use {
                while (it.moveToNext()) {
                    val not = Not(
                        it.getInt(it.getColumnIndex("id")),
                        it.getString(it.getColumnIndex("baslik")),
                        it.getString(it.getColumnIndex("notMetin")),
                        it.getString(it.getColumnIndex("listName")),
                        it.getString(it.getColumnIndex("imageUrl")),
                        it.getString(it.getColumnIndex("color")),
                        it.getString(it.getColumnIndex("tarih"))
                    )
                    notlar.add(not)
                }
            }
        }
        return notlar
    }

    fun notGuncelle(id: Int, not: Not) {
        writableDatabase.use { db ->
            val updateQuery = """
                UPDATE Notlar SET baslik='${not.baslik}', notMetin='${not.notMetin}', 
                listName='${not.listName}', imageUrl='${not.imageUrl}', 
                color='${not.renk}', tarih='${not.tarih}' WHERE id=$id
            """.trimIndent()
            db.execSQL(updateQuery)
        }
    }

    fun notSil(not: Not) {
        writableDatabase.use { db ->
            db.delete("Notlar", "id=?", arrayOf(not.id.toString()))
        }
    }

    @SuppressLint("Range")
    fun notAra(aramaKelimesi: String): ArrayList<Not> {
        val notlar = ArrayList<Not>()
        readableDatabase.use { db ->
            val cursor = db.rawQuery("SELECT * FROM Notlar WHERE baslik LIKE '%$aramaKelimesi%'", null)
            cursor.use {
                while (it.moveToNext()) {
                    val not = Not(
                        it.getInt(it.getColumnIndex("id")),
                        it.getString(it.getColumnIndex("baslik")),
                        it.getString(it.getColumnIndex("notMetin")),
                        it.getString(it.getColumnIndex("listName")),
                        it.getString(it.getColumnIndex("imageUrl")),
                        it.getString(it.getColumnIndex("color")),
                        it.getString(it.getColumnIndex("tarih"))
                    )
                    notlar.add(not)
                }
            }
        }
        return notlar
    }
    @SuppressLint("Range")
    fun getNotById(id: Int): Not? {
        var not: Not? = null
        readableDatabase.use { db ->
            val cursor = db.rawQuery("SELECT * FROM Notlar WHERE id = $id", null)
            cursor.use {
                if (it.moveToFirst()) {
                    not = Not(
                        it.getInt(it.getColumnIndex("id")),
                        it.getString(it.getColumnIndex("baslik")),
                        it.getString(it.getColumnIndex("notMetin")),
                        it.getString(it.getColumnIndex("listName")),
                        it.getString(it.getColumnIndex("imageUrl")),
                        it.getString(it.getColumnIndex("color")),
                        it.getString(it.getColumnIndex("tarih"))
                    )
                }
            }
        }
        return not
    }
}
