package com.example.qrkodolusturucujava.data.numara_kayit;

import android.content.Context;
import android.content.SharedPreferences;

public class NumaraKayitSp {

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public NumaraKayitSp(Context context) {
        sp = context.getSharedPreferences("kullanici_bilgileri", Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public void numaraKeydedildi() {
        editor.putBoolean("kullaniciNumarasiKayitliMi", true).apply();
    }

    public boolean numaraKaydedildiMi() {
        return sp.getBoolean("kullaniciNumarasiKayitliMi", false);
    }

    public String numaraGetir() {
        return sp.getString("numara", "");
    }

    public void numaraKaydet(String kullaniciNumarasi) {
        editor.putString("numara", kullaniciNumarasi).apply();
        numaraKeydedildi();
    }
}
