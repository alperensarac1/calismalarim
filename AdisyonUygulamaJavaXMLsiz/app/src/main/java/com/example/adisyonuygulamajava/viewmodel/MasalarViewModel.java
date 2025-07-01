package com.example.adisyonuygulamajava.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.services.Services;
import com.example.adisyonuygulamajava.services.retrofit.ServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MasalarViewModel extends ViewModel {

    private final MutableLiveData<List<Masa>> _masalar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _birlesmeSonucu = new MutableLiveData<>(false);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Services dao = ServiceImpl.getInstance();

    public LiveData<List<Masa>> getMasalar() {
        return _masalar;
    }

    public LiveData<Boolean> getBirlesmeSonucu() {
        return _birlesmeSonucu;
    }

    public void masalariYukle() {
        executor.execute(() -> {
            try {
                List<Masa> liste = dao.masalariGetir();
                _masalar.postValue(liste);
            } catch (Exception e) {
                Log.e("MasalarVM", "Yükleme hatası: " + e.getLocalizedMessage());
            }
        });
    }

    public void guncelleMasa(Masa masa) {
        List<Masa> mevcutlar = _masalar.getValue();
        if (mevcutlar == null) return;

        List<Masa> yeniListe = new ArrayList<>(mevcutlar);
        int index = -1;
        for (int i = 0; i < yeniListe.size(); i++) {
            if (yeniListe.get(i).getId() == masa.getId()) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            yeniListe.set(index, masa);
            _masalar.setValue(yeniListe);
        }
    }

    public void masaEkle(Runnable onSuccess) {
        executor.execute(() -> {
            try {
                dao.masaEkle();
                onSuccess.run(); // başarılıysa callback çalıştır
            } catch (Exception e) {
                Log.e("MasaVM", "Hata: " + e.getLocalizedMessage());
            }
        });
    }

    public void masaSil(int masaId) {
        executor.execute(() -> {
            try {
                dao.masaSil(masaId);
            } catch (Exception e) {
                Log.e("MasaVM", "Hata", e);
            }
        });
    }

    public void masaBirlestir(int anaId, int bId) {
        executor.execute(() -> {
            try {
                dao.masaBirlestir(anaId, bId);
                _birlesmeSonucu.postValue(true);
            } catch (Exception ignored) {
            }
        });
    }
}

