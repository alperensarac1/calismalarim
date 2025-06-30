package com.example.qrkodolusturucujava.services.dummyservice;

import com.example.qrkodolusturucujava.model.CRUDCevap;
import com.example.qrkodolusturucujava.model.KodUretCevap;
import com.example.qrkodolusturucujava.model.Urun;
import com.example.qrkodolusturucujava.model.UrunCevap;
import com.example.qrkodolusturucujava.model.UrunKategori;
import com.example.qrkodolusturucujava.services.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KahveDummyServisDao implements Services {

    @Override
    public void kullaniciEkle(String kisiTel, Callback<CRUDCevap> callback) {
        CRUDCevap cevap = new CRUDCevap(1, "Ekleme başarılı", 0, 0);
        callback.onResponse(null, Response.success(cevap));
    }

    @Override
    public void tumKahveler(Callback<UrunCevap> callback) {
        UrunCevap cevap = new UrunCevap(Urunler.dummyUrunler);
        callback.onResponse(null, Response.success(cevap));
    }

    @Override
    public void kodUret(String dogrulamaKodu, String kisiTel, Callback<KodUretCevap> callback) {
        KodUretCevap cevap = new KodUretCevap(1, UUID.randomUUID().toString());
        callback.onResponse(null, Response.success(cevap));
    }

    @Override
    public void kahveWithKategoriId(int kategoriId, Callback<UrunCevap> callback) {
        List<Urun> filtreliListe = new ArrayList<>();
        for (Urun urun : Urunler.dummyUrunler) {
            if (urun.getUrunKategoriId() == kategoriId) {
                filtreliListe.add(urun);
            }
        }
        UrunCevap cevap = new UrunCevap(filtreliListe);
        callback.onResponse(null, Response.success(cevap));
    }

    @Override
    public void kodSil(String dogrulamaKodu, Callback<KodUretCevap> callback) {
        KodUretCevap cevap = new KodUretCevap(1, "Kod başarıyla silindi");
        callback.onResponse(null, Response.success(cevap));
    }
}

