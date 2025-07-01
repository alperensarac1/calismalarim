package com.example.adisyonuygulamajava.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.adisyonuygulamajava.model.Kategori;
import com.example.adisyonuygulamajava.model.Urun;
import com.example.adisyonuygulamajava.services.response.KategoriSilResponse;
import com.example.adisyonuygulamajava.services.response.UrunSilResponse;
import com.example.adisyonuygulamajava.services.retrofit.AdisyonServisDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UrunViewModel extends ViewModel {

    private final AdisyonServisDao dao = new AdisyonServisDao();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Urun>> _urunler = new MutableLiveData<>();
    public LiveData<List<Urun>> getUrunler() {
        return _urunler;
    }

    private final MutableLiveData<List<Kategori>> _kategoriler = new MutableLiveData<>();
    public LiveData<List<Kategori>> getKategoriler() {
        return _kategoriler;
    }

    private final MutableLiveData<Boolean> _sonuc = new MutableLiveData<>();
    public LiveData<Boolean> getSonuc() {
        return _sonuc;
    }

    private final MutableLiveData<UrunSilResponse> _silmeSonucu = new MutableLiveData<>();
    public LiveData<UrunSilResponse> getSilmeSonucu() {
        return _silmeSonucu;
    }

    private final MutableLiveData<KategoriSilResponse> _kategoriSilSonuc = new MutableLiveData<>();
    public LiveData<KategoriSilResponse> getKategoriSilSonuc() {
        return _kategoriSilSonuc;
    }

    public void urunleriYukle() {
        executor.execute(() -> {
            try {
                List<Urun> urunList = dao.urunleriGetir();
                _urunler.postValue(urunList);
            } catch (Exception e) {
                Log.e("UrunVM", "Ürün yüklenemedi: " + e.getLocalizedMessage());
            }
        });
    }

    public void urunEkle(String ad, float fiyat, String resimBase64, int kategoriId) {
        executor.execute(() -> {
            try {
                dao.urunEkle(ad, fiyat, kategoriId, 1, resimBase64);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void kategorileriYukle() {
        executor.execute(() -> {
            try {
                List<Kategori> list = dao.kategorileriGetir();
                _kategoriler.postValue(list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void kategoriEkle(String ad) {
        executor.execute(() -> {
            try {
                dao.kategoriEkle(ad);
                _sonuc.postValue(true);
            } catch (Exception e) {
                e.printStackTrace();
                _sonuc.postValue(false);
            }
        });
    }

    public void kategoriSil(int id) {
        executor.execute(() -> {
            try {
                KategoriSilResponse response = dao.kategoriSil(id);
                _kategoriSilSonuc.postValue(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void urunSil(String ad) {
        executor.execute(() -> {
            try {
                UrunSilResponse response = dao.urunSil(ad);
                _silmeSonucu.postValue(response);
                    List<Urun> updatedList = dao.urunleriGetir();
                    _urunler.postValue(updatedList);
            } catch (Exception e) {
                UrunSilResponse fail = new UrunSilResponse(false, e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Bilinmeyen hata");
                _silmeSonucu.postValue(fail);
            }
        });
    }
}

