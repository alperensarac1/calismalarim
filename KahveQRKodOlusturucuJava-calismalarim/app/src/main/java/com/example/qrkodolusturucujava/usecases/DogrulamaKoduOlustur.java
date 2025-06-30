package com.example.qrkodolusturucujava.usecases;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.UUID;

public class DogrulamaKoduOlustur {

    private MutableLiveData<String> _dogrulamaKoduLiveData = new MutableLiveData<>();

    public LiveData<String> getDogrulamaKoduLiveData() {
        return _dogrulamaKoduLiveData;
    }

    public void dogrulamaKodunuOlustur() {
        _dogrulamaKoduLiveData.postValue(randomIdOlustur());
    }

    public String randomIdOlustur() {
        return UUID.randomUUID().toString();
    }
}
