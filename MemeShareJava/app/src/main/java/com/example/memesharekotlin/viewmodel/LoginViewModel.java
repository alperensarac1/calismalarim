package com.example.memesharekotlin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memesharekotlin.model.KullaniciResponse;
import com.example.memesharekotlin.service.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<KullaniciResponse> loginResult = new MutableLiveData<>();

    public LiveData<KullaniciResponse> getLoginResult() {
        return loginResult;
    }

    public void loginUser(String username, String password) {
        ApiClient.getService().loginUser(username, password).enqueue(new Callback<KullaniciResponse>() {
            @Override
            public void onResponse(Call<KullaniciResponse> call, Response<KullaniciResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loginResult.setValue(response.body());
                } else {
                    loginResult.setValue(new KullaniciResponse(false, "Giriş başarısız", -1));
                }
            }

            @Override
            public void onFailure(Call<KullaniciResponse> call, Throwable t) {
                loginResult.setValue(new KullaniciResponse(false, "Bağlantı hatası: " + t.getMessage(), -1));
            }
        });
    }
}

