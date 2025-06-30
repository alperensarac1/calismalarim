package com.example.isletimsistemicalismasi.anaekran.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.isletimsistemicalismasi.anaekran.model.UygulamalarModel;

public class UygulamalarRepository {
    private SharedPreferences.Editor editor;
    private SharedPreferences sp;
    private Context context;

    public UygulamalarRepository(Context mContext){
        this.context = mContext;
        sp =  mContext.getSharedPreferences("copKutusu",Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public void copKutusunaTasi(UygulamalarModel uygulama){
        editor.putBoolean(uygulama.getUygulamaAdi().replace(" ","") + "Cop",true);
        editor.apply();
    }
    public void copKutusundanCikar(UygulamalarModel uygulama){
        editor.putBoolean(uygulama.getUygulamaAdi().replace(" ","") + "Cop",false);
        editor.apply();
    }
}
