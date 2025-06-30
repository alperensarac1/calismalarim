package com.example.isletimsistemicalismasi.anaekran.entity;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.anaekran.model.UygulamalarModel;

import java.io.Serializable;
import java.util.ArrayList;

public class Const implements Serializable {
    static ArrayList<UygulamalarModel> uygulamalarListesi = new ArrayList<UygulamalarModel>();
    static ArrayList<UygulamalarModel> uygulamaListesi2 = new ArrayList<>();
    static ArrayList<UygulamalarModel> copUygulamaListesi = new ArrayList<>();
    private SharedPreferences sp;
    public Const(Context mContext){

        sp =  mContext.getSharedPreferences("copKutusu",Context.MODE_PRIVATE);

        UygulamalarModel copKutusu = new UygulamalarModel("Çöp Kutusu","ic_delete",1,false);
        UygulamalarModel telefonRehberi = new UygulamalarModel("Rehber","ic_person",R.id.toRehberFragment,sp.getBoolean("RehberCop",false));
        UygulamalarModel hesapMakinesi = new UygulamalarModel("Hesap Makinesi","ic_hesapmakinesi",R.id.toHesapMakinesiFragment,sp.getBoolean("HesapMakinesiCop",false));
        UygulamalarModel mesajlasma = new UygulamalarModel("Mesajlaşma","ic_message",R.id.toMesajlasmaFragment,sp.getBoolean("MesajlaşmaCop",false));
        UygulamalarModel telefon = new UygulamalarModel("Telefon","ic_phone", R.id.toTelefonFragment,sp.getBoolean("TelefonCop",false));
        UygulamalarModel tarayici = new UygulamalarModel("Tarayıcı","ic_browser",R.id.toTarayiciFragment,sp.getBoolean("TarayıcıCop",false));
        UygulamalarModel alarm = new UygulamalarModel("Alarm","ic_alarm",R.id.toAlarmFragment,sp.getBoolean("AlarmCop",false));
        UygulamalarModel muzikCalar = new UygulamalarModel("Müzik Çalar","ic_music",R.id.toMuzikCalarFragment,sp.getBoolean("MüzikÇalarCop",false));
        UygulamalarModel kamera = new UygulamalarModel("Kamera","ic_camera",R.id.toKameraFragment,sp.getBoolean("KameraCop",false));
        UygulamalarModel galeri = new UygulamalarModel("Galeri","ic_gallery",R.id.toGaleriFragment,sp.getBoolean("GaleriCop",false));
        UygulamalarModel takvim = new UygulamalarModel("Takvim","ic_calendar",R.id.toTakvimFragment,sp.getBoolean("TakvimCop",false));
        UygulamalarModel qrKodOkuyucu = new UygulamalarModel("QR Okuyucu","ic_qr",R.id.toQrKodOkuyucuFragment,sp.getBoolean("QROkuyucuCop",false));
        UygulamalarModel notDefteri = new UygulamalarModel("Not Defteri","ic_notes",R.id.toNotDefteriFragment,sp.getBoolean("NotDefteriCop",false));

        uygulamalarListesi.add(copKutusu);
        uygulamalarListesi.add(telefonRehberi);
        uygulamalarListesi.add(notDefteri);
        uygulamalarListesi.add(hesapMakinesi);
        uygulamalarListesi.add(mesajlasma);
        uygulamalarListesi.add(telefon);
        uygulamalarListesi.add(alarm);
        uygulamalarListesi.add(kamera);
        uygulamalarListesi.add(tarayici);
        uygulamalarListesi.add(muzikCalar);
        uygulamalarListesi.add(galeri);
        uygulamalarListesi.add(takvim);
        uygulamalarListesi.add(qrKodOkuyucu);

        uygulamalarListesi.forEach(uygulamalarModel -> {
            if (uygulamalarModel.isCopKutusundaMi()){
                copUygulamaListesi.add(uygulamalarModel);
            }
            else{
                uygulamaListesi2.add(uygulamalarModel);
            }
        });
    }
    public static void uygulamalariYukle(){
        uygulamaListesi2.clear();
        copUygulamaListesi.clear();

        uygulamalarListesi.forEach(uygulamalarModel -> {
            if (!uygulamalarModel.isCopKutusundaMi()){
                uygulamaListesi2.add(uygulamalarModel);
            }
            else{
                copUygulamaListesi.add(uygulamalarModel);
            }
        });
    }

    public ArrayList<UygulamalarModel> getTumUygulamalarListesi(){
        return uygulamalarListesi;
    }

    public ArrayList<UygulamalarModel> getCopUygulamaListesi() {
        return copUygulamaListesi;
    }

    public ArrayList<UygulamalarModel> getUygulamalarListesi() {

        return uygulamaListesi2;
    }
}
