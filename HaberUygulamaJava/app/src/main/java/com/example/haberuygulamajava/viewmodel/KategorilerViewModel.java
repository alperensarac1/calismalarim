package com.example.haberuygulamajava.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.haberuygulamajava.dao.HaberDao;
import com.example.haberuygulamajava.model.HaberModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KategorilerViewModel extends ViewModel {

    private HaberDao haberDao;

    private MutableLiveData<List<HaberModel>> _kategoriHaberleri = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<HaberModel>> getKategoriHaberleri() {
        return _kategoriHaberleri;
    }

    public KategorilerViewModel(HaberDao haberDao) {
        this.haberDao = haberDao;
    }

    public void loadKategoriHaberleri(int turId) {
        haberDao.getHaberler(new Callback<List<HaberModel>>() {
            @Override
            public void onResponse(Call<List<HaberModel>> call, Response<List<HaberModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<HaberModel> filtrelenmis = new ArrayList<>();
                    for (HaberModel haber : response.body()) {
                        if (haber.getTur_id() != null && haber.getTur_id() == turId) {
                            filtrelenmis.add(haber);
                        }
                    }
                    _kategoriHaberleri.setValue(filtrelenmis);
                }
            }

            @Override
            public void onFailure(Call<List<HaberModel>> call, Throwable t) {
                _kategoriHaberleri.setValue(new ArrayList<>());
            }
        });
    }
}

