package com.example.qrkodolusturucujava.services.retrofit;

import com.example.qrkodolusturucujava.model.CRUDCevap;
import com.example.qrkodolusturucujava.model.KodUretCevap;
import com.example.qrkodolusturucujava.model.UrunCevap;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface KahveHTTPServisDaoInterface {

    @FormUrlEncoded
    @POST("kullanici_ekle.php")
    Call<CRUDCevap> kullaniciEkle(@Field("telefon_no") String telefonNo);

    @GET("tum_kahveler.php")
    Call<UrunCevap> tumKahveler(); // Kotlin'de suspend olduğundan coroutine kullanılırdı, Java'da Call döner

    @GET("kahve_with_kategori_id.php")
    Call<UrunCevap> kahveWithKategoriId(@Query("id") int kategoriId);

    @FormUrlEncoded
    @POST("kod_uret.php")
    Call<KodUretCevap> kodUret(@Field("dogrulama_kodu") String dogrulamaKodu,
                               @Field("kullanici_tel") String kullaniciTel);

    @FormUrlEncoded
    @POST("kod_sil.php")
    Call<KodUretCevap> kodSil(@Field("dogrulama_kodu") String dogrulamaKodu);
}

