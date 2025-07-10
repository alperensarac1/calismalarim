package com.example.chatjava.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    private static final String PREF_NAME = "mesajlasma_pref";
    private static final String KEY_KULLANICI_ID = "kullanici_id";

    private SharedPreferences prefs;

    public PrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void kaydetKullaniciId(int id) {
        prefs.edit().putInt(KEY_KULLANICI_ID, id).apply();
    }

    public int getirKullaniciId() {
        return prefs.getInt(KEY_KULLANICI_ID, -1);
    }

    public void temizleKullanici() {
        prefs.edit().remove(KEY_KULLANICI_ID).apply();
    }

    public boolean kullaniciVarMi() {
        return getirKullaniciId() != -1;
    }
}

