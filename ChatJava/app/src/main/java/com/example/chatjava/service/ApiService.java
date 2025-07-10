package com.example.chatjava.service;

import com.example.chatjava.service.response.KonusulanKisiListResponse;
import com.example.chatjava.service.response.KullaniciListResponse;
import com.example.chatjava.service.response.MesajListResponse;
import com.example.chatjava.service.response.SimpleResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @FormUrlEncoded
    @POST("kullanici-kayit.php")
    Call<SimpleResponse> kullaniciKayit(
            @Field("ad") String ad,
            @Field("numara") String numara
    );

    @FormUrlEncoded
    @POST("mesaj-gonder.php")
    Call<SimpleResponse> mesajGonder(
            @Field("gonderen_id") int gonderenId,
            @Field("alici_id") int aliciId,
            @Field("mesaj_text") String mesajText,
            @Field("resim_var") int resimVar,
            @Field("base64_img") String base64Img
    );

    @GET("mesajlari-getir.php")
    Call<MesajListResponse> mesajlariGetir(
            @Query("gonderen_id") int gonderenId,
            @Query("alici_id") int aliciId
    );

    @GET("konusulan-kullanicilar.php")
    Call<KonusulanKisiListResponse> konusulanKisiler(
            @Query("kullanici_id") int kullaniciId
    );

    @GET("kullanicilari-getir.php")
    Call<KullaniciListResponse> kullanicilariGetir();

    @GET("test-connection.php")
    Call<SimpleResponse> testConnection();
}

