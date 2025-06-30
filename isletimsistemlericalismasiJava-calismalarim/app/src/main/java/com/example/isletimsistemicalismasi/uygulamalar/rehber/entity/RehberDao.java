package com.example.isletimsistemicalismasi.uygulamalar.rehber.entity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.isletimsistemicalismasi.uygulamalar.rehber.model.RehberKisiler;

import java.util.ArrayList;

public class RehberDao {
    RehberVeritabaniYardimcisi vt;

    public RehberDao(RehberVeritabaniYardimcisi vt) {
        this.vt = vt;
    }
    public void kisiEkle(String kisi_isim,String kisi_numara){
        SQLiteDatabase dbx = vt.getWritableDatabase();
        ContentValues degerler = new ContentValues();
        degerler.put("kisi_isim",kisi_isim);
        degerler.put("kisi_numara",kisi_numara);
        dbx.insertOrThrow("rehber",null,degerler);
    }
    public ArrayList<RehberKisiler> tumKisiler(){
        ArrayList<RehberKisiler> tumKisiler = new ArrayList<>();
        SQLiteDatabase dbx = vt.getWritableDatabase();
        Cursor c = dbx.rawQuery("SELECT * FROM rehber",null);
        while (c.moveToNext()){
            @SuppressLint("Range") RehberKisiler kisi = new RehberKisiler(c.getInt(c.getColumnIndex("kisi_id")),
                    c.getString(c.getColumnIndex("kisi_isim"))
                    ,c.getString(c.getColumnIndex("kisi_numara")));
            tumKisiler.add(kisi);
        }
        dbx.close();
        return tumKisiler;
    }
    public void kisiSil(int kisi_id){
        SQLiteDatabase dbx = vt.getWritableDatabase();
        dbx.delete("rehber","kisi_id=?",new String[]{String.valueOf(kisi_id)});
        dbx.close();
    }
    public void kisiGuncelle(int kisi_id,String kisi_isim,String kisi_numara){
        SQLiteDatabase dbx = vt.getWritableDatabase();
        ContentValues degerler = new ContentValues();
        degerler.put("kisi_id",kisi_id);
        degerler.put("kisi_isim",kisi_isim);
        degerler.put("kisi_numara",kisi_numara);
        dbx.update("rehber",degerler,"kisi_id=?",new String[]{String.valueOf(kisi_id)});
        dbx.close();
    }

}
