package com.example.isletimsistemicalismasi.uygulamalar.rehber.entity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RehberVeritabaniYardimcisi extends SQLiteOpenHelper {
    private static final int Surum=1;
    private static String veritabaniAdi = "rehberveritabani";
    public RehberVeritabaniYardimcisi(Context context) {
        super(context, veritabaniAdi, null, Surum);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE rehber (kisi_id INTEGER PRIMARY KEY AUTOINCREMENT,kisi_isim TEXT,kisi_numara TEXT);");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        db.execSQL("DROP TABLE IF EXISTS kelimeler");
        onCreate(db);
    }
}
