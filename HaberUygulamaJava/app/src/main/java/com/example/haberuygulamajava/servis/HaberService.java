package com.example.haberuygulamajava.servis;

import com.example.haberuygulamajava.model.HaberModel;
import com.example.haberuygulamajava.model.HaberTuruModel;
import com.example.haberuygulamajava.model.YorumInsertRequest;
import com.example.haberuygulamajava.model.YorumModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Query;

public interface HaberService {

    @GET("haber_haberler-get.php")
    Call<List<HaberModel>> getHaberler();

    @GET("haber_yorumlar-get.php")
    Call<List<YorumModel>> getYorumlar(@Query("haber_id") int haberId);

    @POST("haber_yorumlar-insert.php")
    Call<ApiResponse> insertYorum(@Body YorumInsertRequest request);

    @GET("haber_haberturleri-get.php")
    Call<List<HaberTuruModel>> getKategoriler();

    @GET("haber_haberler-son3-get.php")
    Call<List<HaberModel>> getSon3Haber();

    @GET("haber_haberler-sondakika-get.php")
    Call<List<HaberModel>> getSonDakikaHaberler();

    @GET("haber_haberler-gundem-get.php")
    Call<List<HaberModel>> getGundemHaberler();
}

