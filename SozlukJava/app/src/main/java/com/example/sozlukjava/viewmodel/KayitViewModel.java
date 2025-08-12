package com.example.sozlukjava.viewmodel;

// KayitViewModel.java
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sozlukjava.dao.SozlukDao;
import com.example.sozlukjava.model.SimpleResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KayitViewModel extends ViewModel {
    private final SozlukDao dao = new SozlukDao();
    private final MutableLiveData<SimpleResponse> registerResult = new MutableLiveData<>();

    public MutableLiveData<SimpleResponse> getRegisterResult() {
        return registerResult;
    }

    public void register(String username, String password, String email) {
        dao.register(username, password, email).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    registerResult.setValue(response.body());
                } else {
                    SimpleResponse sr = new SimpleResponse();
                    sr.setSuccess(false);
                    sr.setMessage("Sunucu yanıtı geçersiz");
                    registerResult.setValue(sr);
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                SimpleResponse sr = new SimpleResponse();
                sr.setSuccess(false);
                sr.setMessage("Bağlantı hatası");
                registerResult.setValue(sr);
            }
        });
    }
}

