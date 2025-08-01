package com.example.haberuygulamajava.dao;

import com.example.haberuygulamajava.model.HaberModel;
import com.example.haberuygulamajava.model.HaberTuruModel;
import com.example.haberuygulamajava.model.YorumInsertRequest;
import com.example.haberuygulamajava.model.YorumModel;
import com.example.haberuygulamajava.servis.ApiResponse;
import com.example.haberuygulamajava.servis.HaberService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.List;

public class HaberDao {

    private HaberService service;

    public HaberDao(Retrofit retrofit) {
        service = retrofit.create(HaberService.class);
    }

    public void getHaberler(Callback<List<HaberModel>> callback) {
        service.getHaberler().enqueue(callback);
    }

    public void getYorumlar(int haberId, Callback<List<YorumModel>> callback) {
        service.getYorumlar(haberId).enqueue(callback);
    }

    public void insertYorum(YorumInsertRequest yorum, Callback<ApiResponse> callback) {
        service.insertYorum(yorum).enqueue(callback);
    }

    public void getKategoriler(Callback<List<HaberTuruModel>> callback) {
        service.getKategoriler().enqueue(callback);
    }

    public void getSon3Haber(Callback<List<HaberModel>> callback) {
        service.getSon3Haber().enqueue(callback);
    }

    public void getSonDakikaHaberler(Callback<List<HaberModel>> callback) {
        service.getSonDakikaHaberler().enqueue(callback);
    }

    public void getGundemHaberler(Callback<List<HaberModel>> callback) {
        service.getGundemHaberler().enqueue(callback);
    }
}

