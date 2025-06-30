package com.example.isletimsistemicalismasi.uygulamalar.notdefteri.model;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import java.util.ArrayList;

public class NoteDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "NotlarDB";
    private static final int DATABASE_VERSION = 1;

    public NoteDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_NOT = "CREATE TABLE IF NOT EXISTS Notlar (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "baslik TEXT, " +
                "notMetin TEXT, " +
                "listName TEXT, " +
                "imageUrl TEXT, " +
                "color TEXT, " +
                "tarih TEXT)";
        db.execSQL(CREATE_NOT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS Notlar");
        onCreate(db);
    }

    public void yeniNot(Not not) {
        SQLiteDatabase db = this.getWritableDatabase();
        String INSERT_NOT = "INSERT INTO Notlar (baslik, notMetin, listName, imageUrl, color, tarih) " +
                "VALUES ('" + not.getBaslik() + "', '" + not.getNotMetin() + "', '" +
                not.getListName() + "', '" + not.getImageUrl() + "', '" + not.getRenk() + "', '" + not.getTarih() + "')";
        db.execSQL(INSERT_NOT);
        db.close();
    }

    @SuppressLint("Range")
    public ArrayList<Not> getNotlarim() {
        SQLiteDatabase db = this.getWritableDatabase();
        String SELECT_NOT = "SELECT * FROM Notlar ORDER BY id DESC";
        Cursor cursor = db.rawQuery(SELECT_NOT, null);
        ArrayList<Not> notlar = new ArrayList<>();

           while (cursor.moveToNext()){
               Not not = new Not(
                       cursor.getInt(cursor.getColumnIndex("id")),
                       cursor.getString(cursor.getColumnIndex("baslik")),
                       cursor.getString(cursor.getColumnIndex("notMetin")),
                       cursor.getString(cursor.getColumnIndex("listName")),
                       cursor.getString(cursor.getColumnIndex("imageUrl")),
                       cursor.getString(cursor.getColumnIndex("color")),
                       cursor.getString(cursor.getColumnIndex("tarih")));
               notlar.add(not);
           }
      cursor.close();
        return notlar;
    }

    public void NotGuncelle(int id,Not not) {
        SQLiteDatabase db = this.getWritableDatabase();
        String UPDATE_NOT = "UPDATE Notlar SET baslik='" + not.getBaslik() + "', " +
                "notMetin='" + not.getNotMetin() + "', listName='" + not.getListName() + "', " +
                "imageUrl='" + not.getImageUrl() + "', color='" + not.getRenk() + "', " +
                "tarih='" + not.getTarih() + "' WHERE id=" + id;
        db.execSQL(UPDATE_NOT);
        db.close();
    }

    public void NotSil(Not not) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Notlar","id=?",new String[]{String.valueOf(not.getId())});
        db.close();
    }
    public ArrayList<Not> NotAra(String aramaKelimesi){
        ArrayList<Not> notlar = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Notlar WHERE baslik LIKE '%" + aramaKelimesi + "%'",null);
        while (cursor.moveToNext()){
            @SuppressLint("Range") Not not = new Not(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("baslik")),
                    cursor.getString(cursor.getColumnIndex("notMetin")),
                    cursor.getString(cursor.getColumnIndex("listName")),
                    cursor.getString(cursor.getColumnIndex("imageUrl")),
                    cursor.getString(cursor.getColumnIndex("color")),
                    cursor.getString(cursor.getColumnIndex("tarih")));
            notlar.add(not);
        }
        return notlar;
    }
}
