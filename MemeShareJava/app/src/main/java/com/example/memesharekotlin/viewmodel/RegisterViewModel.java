package com.example.memesharekotlin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memesharekotlin.model.KullaniciResponse;
import com.example.memesharekotlin.service.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterViewModel extends ViewModel {

    private final MutableLiveData<KullaniciResponse> registerResult = new MutableLiveData<>();

    public LiveData<KullaniciResponse> getRegisterResult() {
        return registerResult;
    }

    public void registerUser(String username, String password) {
        ApiClient.getService().registerUser(username,password).enqueue(new Callback<KullaniciResponse>() {
            @Override
            public void onResponse(Call<KullaniciResponse> call, Response<KullaniciResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    registerResult.setValue(response.body());
                } else {
                    registerResult.setValue(new KullaniciResponse(false, "Sunucu hatası", -1));
                }
            }

            @Override
            public void onFailure(Call<KullaniciResponse> call, Throwable t) {
                registerResult.setValue(new KullaniciResponse(false, "Bağlantı hatası: " + t.getMessage(), -1));
            }
        });
    }
}

