package com.example.adisyonuygulamakotlin.services.retrofit

import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.model.MasaUrun
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.services.response.KategoriSilResponse
import com.example.adisyonuygulamakotlin.services.response.UrunSilResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

private const val TAG = "AdisyonHTTPServisDao"
interface AdisyonServiceInterface {

    @GET("masa_listesi.php")
    suspend fun getMasalar(): List<Masa>

    @GET("masa_urunleri.php")
    suspend fun getMasaUrunleri(@Query("masa_id") masaId: Int): List<MasaUrun>

    @FormUrlEncoded
    @POST("masa_sil.php")
    suspend fun masaSil(
        @Field("masa_id") masaId: Int
    ): ResponseBody

    @FormUrlEncoded
    @POST("urun_ekle.php")
    suspend fun urunEkle(
        @Field("urun_ad") urunAd: String,
        @Field("urun_fiyat") urunFiyat: Float,
        @Field("urun_kategori") urunKategori: Int,
        @Field("urun_adet") urunAdet: Int,
        @Field("urun_resim") urunResimBase64: String
    ): ResponseBody

    @FormUrlEncoded
    @POST("urun_sil.php")
    suspend fun urunSil(
        @Field("urun_ad") urunAd: String
    ): UrunSilResponse

    @GET("masa_urun_ekle.php")
    suspend fun urunEkle(
        @Query("masa_id") masaId: Int,
        @Query("urun_id") urunId: Int,
        @Query("adet") adet: Int
    ): ResponseBody

    @FormUrlEncoded
    @POST("masa_birlestir.php")
    suspend fun masaBirlestir(
        @Field("ana_masa_id") anaMasaId: Int,
        @Field("birlestirilecek_masa_id") birlestirilecekMasaId: Int
    ): ResponseBody

    @FormUrlEncoded
    @POST("masa_ekle.php")
    suspend fun masaEkle(): ResponseBody


    @GET("urunleri_getir.php")
    suspend fun getUrunler(): List<Urun>

    @FormUrlEncoded
    @POST("masa_odeme.php")
    suspend fun masaOde(
        @Field("masa_id") masaId: Int
    ): ResponseBody

    @GET("masa_toplam_fiyat.php")
    suspend fun getToplamFiyat(@Query("masa_id") masaId: Int): Map<String, Float>

    @FormUrlEncoded
    @POST("urun_cikar.php")
    suspend fun urunCikar(
        @Field("masa_id") masaId: Int,
        @Field("urun_id") urunId: Int
    ): ResponseBody

    @GET("kategorileri_getir.php")
    suspend fun getKategoriler(): ArrayList<Kategori>

    @GET("masa_getir.php")
    suspend fun masaGetir(@Query("masa_id") masaId: Int): Masa


    @FormUrlEncoded
    @POST("kategori_ekle.php")
    suspend fun kategoriEkle(
        @Field("kategori_ad") kategoriAd: String
    ): ResponseBody

    // Kategori sil:
    @FormUrlEncoded
    @POST("kategori_sil.php")
    suspend fun kategoriSil(@Field("kategori_id") id: Int): KategoriSilResponse
}

