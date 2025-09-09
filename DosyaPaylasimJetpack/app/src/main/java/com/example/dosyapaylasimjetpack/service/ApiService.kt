package com.example.dosyapaylasimjetpack.service

import com.example.dosyapaylasimkotlin.model.LinkResponse
import com.example.dosyapaylasimkotlin.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Call

import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @Multipart
    @POST("upload.php")
    fun uploadFile(
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>

    @GET("get-link.php")
    fun getLink(@Query("code") code: String): Call<LinkResponse>
}