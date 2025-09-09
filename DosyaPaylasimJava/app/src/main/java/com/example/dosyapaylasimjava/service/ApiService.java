package com.example.dosyapaylasimjava.service;


import com.example.dosyapaylasimjava.model.LinkResponse;
import com.example.dosyapaylasimjava.model.UploadResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    @Multipart
    @POST("upload.php")
    Call<UploadResponse> uploadFile(@Part MultipartBody.Part file);

    @GET("get-link.php")
    Call<LinkResponse> getLink(@Query("code") String code);
}

