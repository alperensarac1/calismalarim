package com.example.chatkotlin.service

import com.example.chatkotlin.service.response.KonusulanKisiListResponse
import com.example.chatkotlin.service.response.KullaniciListResponse
import com.example.chatkotlin.service.response.MesajListResponse
import com.example.chatkotlin.service.response.SimpleResponse
import retrofit2.http.*
import retrofit2.Call

interface ApiService {

    @FormUrlEncoded
    @POST("kullanici-kayit.php")
    suspend fun kullaniciKayit(
        @Field("ad") ad: String,
        @Field("numara") numara: String
    ): SimpleResponse

    @FormUrlEncoded
    @POST("mesaj-gonder.php")
    suspend fun mesajGonder(
        @Field("gonderen_id") gonderenId: Int,
        @Field("alici_id") aliciId: Int,
        @Field("mesaj_text") mesajText: String,
        @Field("resim_var") resimVar: Int,
        @Field("base64_img") base64Img: String? = null
    ): SimpleResponse

    @GET("mesajlari-getir.php")
    suspend fun mesajlariGetir(
        @Query("gonderen_id") gonderenId: Int,
        @Query("alici_id") aliciId: Int
    ): MesajListResponse

    @GET("konusulan-kullanicilar.php")
    suspend fun konusulanKisiler(
        @Query("kullanici_id") kullaniciId: Int
    ): KonusulanKisiListResponse

    @GET("kullanicilari-getir.php")
    suspend fun kullanicilariGetir(): KullaniciListResponse

    @GET("test-connection.php")
    suspend fun testConnection(): SimpleResponse
}


