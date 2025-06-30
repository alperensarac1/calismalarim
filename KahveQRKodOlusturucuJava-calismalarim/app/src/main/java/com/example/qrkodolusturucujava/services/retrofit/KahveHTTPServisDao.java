package com.example.qrkodolusturucujava.services.retrofit;

import com.example.qrkodolusturucujava.model.CRUDCevap;
import com.example.qrkodolusturucujava.model.KodUretCevap;
import com.example.qrkodolusturucujava.model.UrunCevap;
import com.example.qrkodolusturucujava.services.Services;

import retrofit2.Call;
import retrofit2.Callback;

public class KahveHTTPServisDao implements Services {
    private KahveHTTPServisDaoInterface kdi;

    public KahveHTTPServisDao() {
        kdi = ApiUtils.getKahveHTTPServisDaoInterface();
    }

    @Override
    public void kullaniciEkle(String kisiTel, Callback<CRUDCevap> callback) {
        Call<CRUDCevap> call = kdi.kullaniciEkle(kisiTel);
        call.enqueue(callback);
    }

    @Override
    public void tumKahveler(Callback<UrunCevap> callback) {
        Call<UrunCevap> call = kdi.tumKahveler();
        call.enqueue(callback);
    }

    @Override
    public void kodUret(String dogrulamaKodu, String kisiTel, Callback<KodUretCevap> callback) {
        Call<KodUretCevap> call = kdi.kodUret(dogrulamaKodu, kisiTel);
        call.enqueue(callback);
    }

    @Override
    public void kahveWithKategoriId(int kategoriId, Callback<UrunCevap> callback) {
        Call<UrunCevap> call = kdi.kahveWithKategoriId(kategoriId);
        call.enqueue(callback);
    }

    @Override
    public void kodSil(String dogrulamaKodu, Callback<KodUretCevap> callback) {
        Call<KodUretCevap> call = kdi.kodSil(dogrulamaKodu);
        call.enqueue(callback);
    }
}

