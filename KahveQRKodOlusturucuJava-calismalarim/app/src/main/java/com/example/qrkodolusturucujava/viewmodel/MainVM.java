package com.example.qrkodolusturucujava.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.qrkodolusturucujava.data.numara_kayit.NumaraKayitSp;
import com.example.qrkodolusturucujava.model.CRUDCevap;
import com.example.qrkodolusturucujava.services.Services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class MainVM extends ViewModel {

    private static final String TAG = "MainVM";

    private final Services kullaniciServisDao;
    private final NumaraKayitSp numaraKayitSp;

    private final MutableLiveData<Integer> hediyeKahveLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> kahveSayisiLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> telefonNumarasiLiveData = new MutableLiveData<>();

    public LiveData<Integer> getHediyeKahveLiveData() {
        return hediyeKahveLiveData;
    }

    public LiveData<Integer> getKahveSayisiLiveData() {
        return kahveSayisiLiveData;
    }

    public LiveData<String> getTelefonNumarasiLiveData() {
        return telefonNumarasiLiveData;
    }

    @Inject
    public MainVM(Services kullaniciServisDao, NumaraKayitSp numaraKayitSp) {
        this.kullaniciServisDao = kullaniciServisDao;
        this.numaraKayitSp = numaraKayitSp;
        startPeriodicKullaniciEkle();
    }

    public void kullaniciEkle(String numara) {
        if (numara.isEmpty()) {
            Log.e(TAG, "Telefon numarası boş! Kullanıcı eklenemez.");
            return;
        }

        kullaniciServisDao.kullaniciEkle(numara, new Callback<CRUDCevap>() {
            @Override
            public void onResponse(Call<CRUDCevap> call, Response<CRUDCevap> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CRUDCevap cevap = response.body();
                    Log.d(TAG, "API Yanıtı: " + cevap.getMessage());
                    Log.d(TAG, "API Success: " + cevap.getSuccess());

                    hediyeKahveLiveData.postValue(cevap.getHediyeKahve());
                    kahveSayisiLiveData.postValue(cevap.getKahveSayisi());
                } else {
                    Log.e(TAG, "Yanıt başarısız veya boş");
                }
            }

            @Override
            public void onFailure(Call<CRUDCevap> call, Throwable t) {
                Log.e(TAG, "API isteği başarısız: " + t.getLocalizedMessage());
            }
        });
    }
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private void startPeriodicKullaniciEkle() {
        scheduler.scheduleAtFixedRate(() -> {
            String numara = numaraKayitSp.numaraGetir();

            if (numara != null && !numara.isEmpty()) {
                kullaniciEkle(numara);
            } else {
                Log.e(TAG, "Numara kaydedilmemiş!");
            }

        }, 0, 15, TimeUnit.SECONDS);
    }

    public boolean checkTelefonNumarasi() {
        String savedPhoneNumber = numaraKayitSp.numaraGetir();
        if (savedPhoneNumber == null || savedPhoneNumber.isEmpty()) {
            Log.e(TAG, "Telefon numarası kaydedilmemiş!");
            return false;
        }
        telefonNumarasiLiveData.postValue(savedPhoneNumber);
        kullaniciEkle(savedPhoneNumber);
        return true;
    }

    public void numarayiKaydet(String numara) {
        numaraKayitSp.numaraKaydet(numara);
        Log.e(TAG, "Numara kaydedildi: " + numara);
        telefonNumarasiLiveData.postValue(numara);
    }


}

