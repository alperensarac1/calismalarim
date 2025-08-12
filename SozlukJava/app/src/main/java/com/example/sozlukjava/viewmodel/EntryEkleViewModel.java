package com.example.sozlukjava.viewmodel;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sozlukjava.dao.SozlukDao;
import com.example.sozlukjava.model.SimpleResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntryEkleViewModel extends ViewModel {
    private final SozlukDao dao = new SozlukDao();
    private final MutableLiveData<SimpleResponse> addResult = new MutableLiveData<>();

    public MutableLiveData<SimpleResponse> getAddResult() {
        return addResult;
    }

    public void addEntry(int userId, String title, String content) {
        dao.addEntry(userId, title, content).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                addResult.setValue(response.body());
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                SimpleResponse sr = new SimpleResponse();
                sr.setSuccess(false);
                sr.setMessage("Bağlantı hatası");
                addResult.setValue(sr);
            }
        });
    }
}

