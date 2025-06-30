package com.example.isletimsistemicalismasi.giris_ekrani.repository;

import android.content.Context;
import android.content.SharedPreferences;

public class KullaniciBilgileri {
    Context mContext;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    public KullaniciBilgileri(Context mContext) {
        this.mContext = mContext;
         sp = mContext.getSharedPreferences("KullaniciBilgileri",Context.MODE_PRIVATE);

        editor = sp.edit();
    }
    public boolean sifreKaydiOlusturulmusMu(){
        return sp.getBoolean("kayitolusturulmusmu",false);
    }

    public void sifreKaydi(String sifre){
        editor.putString("sifre",sifre);
        editor.putBoolean("kayitolusturulmusmu",true);
        onayla();
    }
    public boolean sifreSorgulama(String dogrulanacakSifre){
        return dogrulanacakSifre.equals(sp.getString("sifre",""));
    }
    public void guvenlikSorusuKaydet(String guvenlikSorusu){
        editor.putString("guvenliksorusu",guvenlikSorusu);
        onayla();
    }
    public String guvenlikSorusuGetir(){
        return sp.getString("guvenliksorusu","");
    }
    public void guvenlikSorusuCevapKaydet(String guvenlikSorusuCevap){
        editor.putString("guvenliksorusucevap",guvenlikSorusuCevap);
        onayla();
    }
    public boolean guvenlikSorusuDogrula(String dogrulanacakCevap){
        return dogrulanacakCevap.equals(sp.getString("guvenliksorusucevap",dogrulanacakCevap));
    }
    public void sifreSorulsunMuDegistir(boolean sifreSorulsunMu){
        editor.putBoolean("sifresorulsunmu",sifreSorulsunMu);
        onayla();
    }
    public boolean sifreSorulsunMu(){
        return sp.getBoolean("sifresorulsunmu",true);

    }
    public void sifreDegistir(String degistirilecekSifre){
        editor.putString("sifre",degistirilecekSifre);
        onayla();
    }
    public void kullaniciBilgileriniSifirla(){
        editor.remove("kayitolusturulmusmu");
        editor.remove("sifre");
        editor.remove("guvenliksorusu");
        editor.remove("sifresorulsunmu");
        onayla();
    }
    private void onayla(){
        editor.apply();
    }
}
