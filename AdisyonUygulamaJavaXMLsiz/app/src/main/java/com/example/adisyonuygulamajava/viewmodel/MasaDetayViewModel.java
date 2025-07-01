package com.example.adisyonuygulamajava.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.adisyonuygulamajava.model.Kategori;
import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.model.MasaUrun;
import com.example.adisyonuygulamajava.model.Urun;
import com.example.adisyonuygulamajava.services.retrofit.AdisyonServisDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MasaDetayViewModel extends ViewModel {

    private final int masaId;
    private final AdisyonServisDao dao = new AdisyonServisDao();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<MasaUrun>> _urunler = new MutableLiveData<>();
    private final MutableLiveData<List<Urun>> _tumUrunler = new MutableLiveData<>();
    private final MutableLiveData<List<Kategori>> _kategoriler = new MutableLiveData<>();
    private final MutableLiveData<Float> _toplamFiyat = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    private final MutableLiveData<Masa> _masa = new MutableLiveData<>();
    public final MutableLiveData<Boolean> odemeTamamlandi = new MutableLiveData<>(false);

    public MasaDetayViewModel(int masaId) {
        this.masaId = masaId;
    }

    public LiveData<List<MasaUrun>> getUrunler() {
        return _urunler;
    }

    public LiveData<List<Urun>> getTumUrunler() {
        return _tumUrunler;
    }

    public LiveData<List<Kategori>> getKategoriler() {
        return _kategoriler;
    }

    public LiveData<Float> getToplamFiyat() {
        return _toplamFiyat;
    }

    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    public LiveData<Masa> getMasa() {
        return _masa;
    }

    public void yukleTumVeriler() {
        executor.execute(() -> {
            _isLoading.postValue(true);
            try {
                Masa masa = dao.masaGetir(masaId);
                _masa.postValue(masa);

                List<MasaUrun> masaUrunleri = dao.masaUrunleriniGetir(masaId);
                List<Urun> urunler = dao.urunleriGetir();
                List<Kategori> kategoriler = dao.kategorileriGetir();

                List<Urun> guncellenmisUrunler = new ArrayList<>();
                for (Urun urun : urunler) {
                    int adet = 0;
                    for (MasaUrun mu : masaUrunleri) {
                        if (mu.getUrun_id() == urun.getId()) {
                            adet = mu.getAdet();
                            break;
                        }
                    }
                    urun.setUrun_adet(adet);
                    guncellenmisUrunler.add(urun);
                }

                float toplam = 0f;
                for (MasaUrun mu : masaUrunleri) {
                    toplam += mu.getToplam_fiyat();
                }

                _urunler.postValue(masaUrunleri);
                _tumUrunler.postValue(guncellenmisUrunler);
                _kategoriler.postValue(kategoriler);
                _toplamFiyat.postValue(toplam);

            } catch (Exception e) {
                Log.e("VM", "Veri yüklenemedi: " + e.getLocalizedMessage());
            } finally {
                _isLoading.postValue(false);
            }
        });
    }

    public void urunEkle(int urunId, int adet) {
        executor.execute(() -> {
            try {
                dao.urunEkle(masaId, urunId, adet);
                List<MasaUrun> masaUrunleri = dao.masaUrunleriniGetir(masaId);
                float toplam = 0f;
                for (MasaUrun mu : masaUrunleri) {
                    toplam += mu.getToplam_fiyat();
                }

                _urunler.postValue(masaUrunleri);
                _toplamFiyat.postValue(toplam);
                yukleTumVeriler();
            } catch (Exception e) {
                Log.e("VM", "Ürün ekleme hatası: " + e.getLocalizedMessage());
            }
        });
    }

    public void urunCikar(int urunId) {
        executor.execute(() -> {
            try {
                dao.urunCikar(masaId, urunId);

                List<Urun> urunler = _tumUrunler.getValue();
                if (urunler != null) {
                    List<Urun> guncellenmis = new ArrayList<>();
                    for (Urun u : urunler) {
                        if (u.getId() == urunId && u.getUrun_adet() > 0) {
                            u.setUrun_adet(u.getUrun_adet() - 1);
                        }
                        guncellenmis.add(u);
                    }
                    _tumUrunler.postValue(guncellenmis);
                }

                List<MasaUrun> masaUrunleri = dao.masaUrunleriniGetir(masaId);
                float toplam = 0f;
                for (MasaUrun mu : masaUrunleri) {
                    toplam += mu.getToplam_fiyat();
                }

                _urunler.postValue(masaUrunleri);
                _toplamFiyat.postValue(toplam);
                yukleTumVeriler();
            } catch (Exception e) {
                Log.e("VM", "Ürün çıkarma hatası: " + e.getLocalizedMessage());
            }
        });
    }

    public void odemeAl(Runnable onSuccess) {
        executor.execute(() -> {
            try {
                dao.masaOdemeYap(masaId);

                List<Urun> urunler = _tumUrunler.getValue();
                if (urunler != null) {
                    for (Urun u : urunler) {
                        u.setUrun_adet(0);
                    }
                    _tumUrunler.postValue(urunler);
                }

                _toplamFiyat.postValue(0f);

                // Ödeme tamamlandı
                odemeTamamlandi.postValue(true);

                if (onSuccess != null) onSuccess.run();

            } catch (Exception e) {
                Log.e("VM", "Ödeme alma hatası: " + e.getLocalizedMessage());
            }
        });
    }

}

