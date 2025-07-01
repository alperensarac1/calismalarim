package com.example.adisyonuygulamajava.services;

import com.example.adisyonuygulamajava.model.Kategori;
import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.model.MasaUrun;
import com.example.adisyonuygulamajava.model.Urun;
import com.example.adisyonuygulamajava.services.response.KategoriSilResponse;
import com.example.adisyonuygulamajava.services.response.UrunSilResponse;

import java.util.List;

import okhttp3.ResponseBody;

public interface Services {

    void urunEkle(String urunAd, float fiyat, int kategoriId, int adet, String base64);

    ResponseBody masaSil(int masaId);

    ResponseBody masaEkle();

    List<Masa> masalariGetir();

    List<Urun> urunleriGetir();

    List<MasaUrun> masaUrunleriniGetir(int masaId);

    ResponseBody urunEkle(int masaId, int urunId, int adet);

    ResponseBody masaOdemeYap(int masaId);

    float masaToplamFiyat(int masaId);

    ResponseBody urunCikar(int masaId, int urunId);

    List<Kategori> kategorileriGetir();

    Masa masaGetir(int masaId);

    ResponseBody masaBirlestir(int anaMasaId, int birlestirilecekMasaId);

    ResponseBody kategoriEkle(String ad);

    KategoriSilResponse kategoriSil(int id);

    UrunSilResponse urunSil(String urunAd);
}

