package com.example.adisyonuygulamajava.services.retrofit;

import com.example.adisyonuygulamajava.model.Kategori;
import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.model.MasaUrun;
import com.example.adisyonuygulamajava.model.Urun;
import com.example.adisyonuygulamajava.services.response.KategoriSilResponse;
import com.example.adisyonuygulamajava.services.response.UrunSilResponse;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface AdisyonServiceInterface {

    @GET("masa_listesi.php")
    Call<List<Masa>> getMasalar();

    @GET("masa_urunleri.php")
    Call<List<MasaUrun>> getMasaUrunleri(@Query("masa_id") int masaId);

    @FormUrlEncoded
    @POST("masa_sil.php")
    Call<ResponseBody> masaSil(@Field("masa_id") int masaId);

    @FormUrlEncoded
    @POST("urun_ekle.php")
    Call<ResponseBody> urunEkle(
            @Field("urun_ad") String urunAd,
            @Field("urun_fiyat") float urunFiyat,
            @Field("urun_kategori") int urunKategori,
            @Field("urun_adet") int urunAdet,
            @Field("urun_resim") String urunResimBase64
    );

    @FormUrlEncoded
    @POST("urun_sil.php")
    Call<UrunSilResponse> urunSil(@Field("urun_ad") String urunAd);

    @GET("masa_urun_ekle.php")
    Call<ResponseBody> urunEkle(
            @Query("masa_id") int masaId,
            @Query("urun_id") int urunId,
            @Query("adet") int adet
    );

    @FormUrlEncoded
    @POST("masa_birlestir.php")
    Call<ResponseBody> masaBirlestir(
            @Field("ana_masa_id") int anaMasaId,
            @Field("birlestirilecek_masa_id") int birlestirilecekMasaId
    );

    @FormUrlEncoded
    @POST("masa_ekle.php")
    Call<ResponseBody> masaEkle();

    @GET("urunleri_getir.php")
    Call<List<Urun>> getUrunler();

    @FormUrlEncoded
    @POST("masa_odeme.php")
    Call<ResponseBody> masaOde(@Field("masa_id") int masaId);

    @GET("masa_toplam_fiyat.php")
    Call<Map<String, Float>> getToplamFiyat(@Query("masa_id") int masaId);

    @FormUrlEncoded
    @POST("urun_cikar.php")
    Call<ResponseBody> urunCikar(
            @Field("masa_id") int masaId,
            @Field("urun_id") int urunId
    );

    @GET("kategorileri_getir.php")
    Call<List<Kategori>> getKategoriler();

    @GET("masa_getir.php")
    Call<Masa> masaGetir(@Query("masa_id") int masaId);

    @FormUrlEncoded
    @POST("kategori_ekle.php")
    Call<ResponseBody> kategoriEkle(@Field("kategori_ad") String kategoriAd);

    @FormUrlEncoded
    @POST("kategori_sil.php")
    Call<KategoriSilResponse> kategoriSil(@Field("kategori_id") int id);
}

