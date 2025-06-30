package com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri.model

import android.annotation.SuppressLint
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
            val insertQuery = """
                INSERT INTO Notlar (baslik, notMetin, listName, imageUrl, color, tarih) 
                VALUES ('${not.baslik}', '${not.notMetin}', '${not.listName}', 
                '${not.imageUrl}', '${not.renk}', '${not.tarih}')
            """.trimIndent()
            db.execSQL(insertQuery)
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
}
