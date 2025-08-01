package com.example.haberuygulama.servis

import com.example.haberuygulama.model.HaberModel
import com.example.haberuygulama.model.HaberTuruModel
import com.example.haberuygulama.model.YorumInsertRequest
import com.example.haberuygulama.model.YorumModel
import retrofit2.Call
import retrofit2.http.*


interface HaberService {

    @GET("haber_haberler-get.php")
    fun getHaberler(): Call<List<HaberModel>>


    @GET("haber_yorumlar-get.php")
    fun getYorumlar(@Query("haber_id") haberId: Int): Call<List<YorumModel>>

    @POST("haber_yorumlar-insert.php")
    fun insertYorum(@Body request: YorumInsertRequest): Call<ApiResponse>

    @GET("haber_haberturleri-get.php")
    fun getKategoriler(): Call<List<HaberTuruModel>>

    @GET("haber_haberler-son3-get.php")
    fun getSon3Haber(): Call<List<HaberModel>>

    @GET("haber_haberler-sondakika-get.php")
    fun getSonDakikaHaberler(): Call<List<HaberModel>>

    @GET("haber_haberler-gundem-get.php")
    fun getGundemHaberler(): Call<List<HaberModel>>


}
