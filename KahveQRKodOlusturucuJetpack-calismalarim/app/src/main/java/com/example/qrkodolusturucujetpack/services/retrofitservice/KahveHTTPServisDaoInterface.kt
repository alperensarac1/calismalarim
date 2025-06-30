package com.example.qrkodolusturucujetpack.services.retrofitservice

import com.example.qrkodolusturucu.model.CRUDCevap
import com.example.qrkodolusturucujetpack.model.KodUretCevap
import com.example.qrkodolusturucujetpack.model.UrunCevap
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface KahveHTTPServisDaoInterface {

    @FormUrlEncoded
    @POST("kullanici_ekle.php")
    fun kullaniciEkle(@Field("telefon_no") telefonNo: String): Call<CRUDCevap>


    @GET("tum_kahveler.php")
    suspend fun tumKahveler(): UrunCevap

    @GET("kahve_with_kategori_id.php")
    suspend fun kahveWithKategoriId(
        @Query("id") kategoriId: Int
    ): UrunCevap

    @POST("kod_uret.php")
    @FormUrlEncoded
    fun kodUret(@Field("dogrulama_kodu") dogrulamaKodu:String,@Field("kullanici_tel") kullaniciTel:String):Call<KodUretCevap>

    @POST("kod_sil.php")
    @FormUrlEncoded
    fun kodSil(@Field("dogrulama_kodu") dogrulamaKodu:String):Call<KodUretCevap>


}