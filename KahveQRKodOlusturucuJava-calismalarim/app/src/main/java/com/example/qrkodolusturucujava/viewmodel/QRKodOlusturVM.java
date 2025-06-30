package com.example.qrkodolusturucujava.viewmodel;

import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.qrkodolusturucujava.data.numara_kayit.NumaraKayitSp;
import com.example.qrkodolusturucujava.model.KodUretCevap;
import com.example.qrkodolusturucujava.services.Services;
import com.example.qrkodolusturucujava.usecases.DogrulamaKoduOlustur;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class QRKodOlusturVM extends ViewModel {

    private final Services kahveServisDao;
    private final NumaraKayitSp numaraKayitSp;
    private final DogrulamaKoduOlustur dogrulamaKoduOlustur;

    private final MutableLiveData<String> kalanSureLiveData = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> qrKodLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> dogrulamaKoduLiveData = new MutableLiveData<>();

    private CountDownTimer countDownTimer;

    @Inject
    public QRKodOlusturVM(Services kahveServisDao, NumaraKayitSp numaraKayitSp, DogrulamaKoduOlustur dogrulamaKoduOlustur) {
        this.kahveServisDao = kahveServisDao;
        this.numaraKayitSp = numaraKayitSp;
        this.dogrulamaKoduOlustur = dogrulamaKoduOlustur;
        yeniKodUret();
    }

    public LiveData<String> getKalanSureLiveData() {
        return kalanSureLiveData;
    }

    public LiveData<Bitmap> getQrKodLiveData() {
        return qrKodLiveData;
    }

    public LiveData<String> getDogrulamaKoduLiveData() {
        return dogrulamaKoduLiveData;
    }

    public void yeniKodUret() {
        String yeniKod = dogrulamaKoduOlustur.randomIdOlustur();
        dogrulamaKoduLiveData.postValue(yeniKod);
    }

    public void QRKodOlustur(String dogrulamaKodu) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(dogrulamaKodu, BarcodeFormat.QR_CODE, 400, 400);
            qrKodLiveData.postValue(bitmap);
            Log.d("QRKodOlusturVM", "QR kod başarıyla oluşturuldu.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("QRKodOlusturVM", "QR kod oluşturulurken hata: " + e.getMessage());
        }
    }

    public void koduOlustur(String dogrulamaKodu) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String numara = numaraKayitSp.numaraGetir();
                if (numara == null || numara.isEmpty()) {
                    Log.e("QRKodOlusturVM", "Telefon numarası boş olduğu için kod oluşturulmadı!");
                    return;
                }

                kahveServisDao.kodUret(dogrulamaKodu, numara, new Callback<KodUretCevap>() {
                    @Override
                    public void onResponse(Call<KodUretCevap> call, Response<KodUretCevap> response) {

                    }

                    @Override
                    public void onFailure(Call<KodUretCevap> call, Throwable t) {

                    }
                });
                Log.d("QRKodOlusturVM", "Kod veritabanına eklendi: " + dogrulamaKodu);

                if (countDownTimer != null) countDownTimer.cancel();

                new Handler(Looper.getMainLooper()).post(() -> {
                    countDownTimer = new CountDownTimer(90000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            kalanSureLiveData.postValue(millisUntilFinished / 1000 + " saniye");
                        }

                        @Override
                        public void onFinish() {
                            kodSil(dogrulamaKodu);
                            yeniKodUret();
                        }
                    };
                    countDownTimer.start();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("QRKodOlusturVM", "Kod oluşturulurken hata: " + e.getMessage());
            }
        });
    }

    private void kodSil(String dogrulamaKodu) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                kahveServisDao.kodSil(dogrulamaKodu, new Callback<KodUretCevap>() {
                    @Override
                    public void onResponse(Call<KodUretCevap> call, Response<KodUretCevap> response) {

                    }

                    @Override
                    public void onFailure(Call<KodUretCevap> call, Throwable t) {

                    }
                });
                Log.d("QRKodOlusturVM", "Kod silindi: " + dogrulamaKodu);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("QRKodOlusturVM", "Kod silinirken hata: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
