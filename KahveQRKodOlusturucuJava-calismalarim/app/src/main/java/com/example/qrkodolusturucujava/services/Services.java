package com.example.qrkodolusturucujava.services;

import com.example.qrkodolusturucujava.model.CRUDCevap;
import com.example.qrkodolusturucujava.model.KodUretCevap;
import com.example.qrkodolusturucujava.model.UrunCevap;

import retrofit2.Callback;

public interface Services {
    void kullaniciEkle(String kisiTel, Callback<CRUDCevap> callback);
    void tumKahveler(Callback<UrunCevap> callback);
    void kodUret(String dogrulamaKodu, String kisiTel, Callback<KodUretCevap> callback);
    void kahveWithKategoriId(int kategoriId, Callback<UrunCevap> callback);
    void kodSil(String dogrulamaKodu, Callback<KodUretCevap> callback);
}

