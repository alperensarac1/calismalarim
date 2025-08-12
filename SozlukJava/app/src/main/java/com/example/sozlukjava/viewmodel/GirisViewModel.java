package com.example.sozlukjava.viewmodel;

// GirisViewModel.java
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sozlukjava.dao.SozlukDao;
import com.example.sozlukjava.model.SimpleResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GirisViewModel extends ViewModel {
    private final SozlukDao dao = new SozlukDao();
    private final MutableLiveData<SimpleResponse> loginResult = new MutableLiveData<>();

    public MutableLiveData<SimpleResponse> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        dao.login(username, password).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loginResult.setValue(response.body());
                } else {
                    SimpleResponse sr = new SimpleResponse();
                    sr.setSuccess(false);
                    sr.setMessage("Sunucu yanıtı geçersiz");
                    loginResult.setValue(sr);
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                SimpleResponse sr = new SimpleResponse();
                sr.setSuccess(false);
                sr.setMessage("Bağlantı hatası");
                loginResult.setValue(sr);
            }
        });
    }
}

