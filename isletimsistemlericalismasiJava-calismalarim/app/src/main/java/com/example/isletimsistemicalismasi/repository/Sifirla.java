package com.example.isletimsistemicalismasi.repository;

import android.content.Context;

import com.example.isletimsistemicalismasi.anaekran.entity.Const;
import com.example.isletimsistemicalismasi.anaekran.repository.UygulamalarRepository;
import com.example.isletimsistemicalismasi.giris_ekrani.repository.KullaniciBilgileri;

public class Sifirla {

    KullaniciBilgileri kullaniciBilgileri;
    UygulamalarRepository uygulamalarRepository;
    Const uygulamalar;

    public void sifirla(Context mContext){
        uygulamalar = new Const(mContext);
         kullaniciBilgileri = new KullaniciBilgileri(mContext);
         uygulamalarRepository = new UygulamalarRepository(mContext);
         uygulamalar.getUygulamalarListesi().forEach(uygulamalarModel -> {
             uygulamalarRepository.copKutusundanCikar(uygulamalarModel);
         });
         kullaniciBilgileri.kullaniciBilgileriniSifirla();
    }
}
