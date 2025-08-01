package com.example.haberuygulamajava.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.haberuygulamajava.dao.HaberDao;
import com.example.haberuygulamajava.model.HaberModel;
import com.example.haberuygulamajava.model.HaberTuruModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HaberlerViewModel extends ViewModel {
    private final HaberDao haberDao;

    private final MutableLiveData<List<HaberModel>> tumHaberler = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<HaberModel>> filtrelenmisHaberler = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<HaberTuruModel>> kategoriler = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<HaberModel>> sonHaberler = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<HaberModel>> gundemHaberler = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<HaberModel>> sonDakikaHaberler = new MutableLiveData<>(new ArrayList<>());

    public HaberlerViewModel(HaberDao haberDao) {
        this.haberDao = haberDao;
    }

    public LiveData<List<HaberModel>> getHaberler() {
        return filtrelenmisHaberler;
    }

    public LiveData<List<HaberTuruModel>> getKategoriler() {
        return kategoriler;
    }

    public LiveData<List<HaberModel>> getSonHaberler() {
        return sonHaberler;
    }

    public LiveData<List<HaberModel>> getGundemHaberler() {
        return gundemHaberler;
    }

    public LiveData<List<HaberModel>> getSonDakikaHaberler() {
        return sonDakikaHaberler;
    }

    public void loadData() {
        haberDao.getHaberler(new Callback<List<HaberModel>>() {
            @Override
            public void onResponse(Call<List<HaberModel>> call, Response<List<HaberModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<HaberModel> list = response.body();
                    tumHaberler.setValue(list);
                    filtrelenmisHaberler.setValue(list);
                }
            }

            @Override
            public void onFailure(Call<List<HaberModel>> call, Throwable t) { }
        });

        haberDao.getKategoriler(new Callback<List<HaberTuruModel>>() {
            @Override
            public void onResponse(Call<List<HaberTuruModel>> call, Response<List<HaberTuruModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    kategoriler.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<HaberTuruModel>> call, Throwable t) { }
        });
    }

    public void filtreleKategori(Integer turId) {
        List<HaberModel> tumListe = tumHaberler.getValue();
        if (tumListe == null) return;

        if (turId == null) {
            filtrelenmisHaberler.setValue(tumListe);
        } else {
            List<HaberModel> filtreli = new ArrayList<>();
            for (HaberModel h : tumListe) {
                if (h.getTur_id() != null && h.getTur_id().equals(turId)) {
                    filtreli.add(h);
                }
            }
            filtrelenmisHaberler.setValue(filtreli);
        }
    }

    public void loadSon3Haber() {
        haberDao.getSon3Haber(new Callback<List<HaberModel>>() {
            @Override
            public void onResponse(Call<List<HaberModel>> call, Response<List<HaberModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sonHaberler.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<HaberModel>> call, Throwable t) {
                // Log hata
            }
        });
    }


    public void loadGundemHaberler() {
        haberDao.getGundemHaberler(new Callback<List<HaberModel>>() {
            @Override
            public void onResponse(Call<List<HaberModel>> call, Response<List<HaberModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    gundemHaberler.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<HaberModel>> call, Throwable t) {
                // Log hata
            }
        });
    }

    public void loadSonDakikaHaberler() {
        haberDao.getSonDakikaHaberler(new Callback<List<HaberModel>>() {
            @Override
            public void onResponse(Call<List<HaberModel>> call, Response<List<HaberModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sonDakikaHaberler.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<HaberModel>> call, Throwable t) { }
        });
    }
}

