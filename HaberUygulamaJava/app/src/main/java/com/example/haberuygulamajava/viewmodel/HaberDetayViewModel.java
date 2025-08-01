package com.example.haberuygulamajava.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.haberuygulamajava.dao.HaberDao;
import com.example.haberuygulamajava.model.YorumInsertRequest;
import com.example.haberuygulamajava.model.YorumModel;
import com.example.haberuygulamajava.servis.ApiResponse;
import com.example.haberuygulamajava.servis.HaberService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class HaberDetayViewModel extends ViewModel {
    private final HaberService service;

    private final MutableLiveData<List<YorumModel>> yorumlar = new MutableLiveData<>();

    public HaberDetayViewModel() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://alperensaracdeneme.com/haberservis/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(HaberService.class);
    }

    public LiveData<List<YorumModel>> getYorumlar() {
        return yorumlar;
    }

    public void loadYorumlar(int haberId) {
        service.getYorumlar(haberId).enqueue(new Callback<List<YorumModel>>() {
            @Override
            public void onResponse(Call<List<YorumModel>> call, Response<List<YorumModel>> response) {
                if (response.isSuccessful()) {
                    yorumlar.postValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<YorumModel>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void yorumEkle(int haberId, String takmaAd, String yorumMetni) {
        YorumInsertRequest request = new YorumInsertRequest(haberId, takmaAd, yorumMetni);

        service.insertYorum(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    loadYorumlar(haberId);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}


