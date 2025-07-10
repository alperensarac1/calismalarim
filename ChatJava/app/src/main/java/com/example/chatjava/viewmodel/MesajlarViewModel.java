package com.example.chatjava.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatjava.model.Mesaj;
import com.example.chatjava.service.ApiService;
import com.example.chatjava.service.RetrofitClient;
import com.example.chatjava.service.response.MesajListResponse;
import com.example.chatjava.service.response.SimpleResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MesajlarViewModel extends ViewModel {

    /* ---------------- live-data ---------------- */

    private final MutableLiveData<List<Mesaj>> _mesajlar = new MutableLiveData<>();

    public LiveData<List<Mesaj>> getMesajlar() {
        return _mesajlar;
    }

    private final MutableLiveData<String> _hataMesaji = new MutableLiveData<>();

    public LiveData<String> getHataMesaji() {
        return _hataMesaji;
    }


    private final ApiService api = RetrofitClient.getApiService();


    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable mesajGuncelleRunnable;

    /**
     * İlk çağrıda hemen, ardından her 15 sn’de bir mesaj listesi çeker.
     */
    public void mesajlariYuklePeriyodik(int gonderenId, int aliciId) {
        // Var olan döngüyü iptal et
        if (mesajGuncelleRunnable != null) {
            handler.removeCallbacks(mesajGuncelleRunnable);
        }

        mesajGuncelleRunnable = new Runnable() {
            @Override
            public void run() {
                yukleMesajlar(gonderenId, aliciId);
                handler.postDelayed(this, 15_000); // 15 sn sonra tekrar
            }
        };
        handler.post(mesajGuncelleRunnable); // hemen çalıştır
    }

    private void yukleMesajlar(int gonderenId, int aliciId) {
        api.mesajlariGetir(gonderenId, aliciId)
                .enqueue(new Callback<MesajListResponse>() {
                    @Override
                    public void onResponse(Call<MesajListResponse> call,
                                           Response<MesajListResponse> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            _mesajlar.postValue(response.body().getMesajlar());
                            _hataMesaji.postValue(null);
                        } else {
                            _hataMesaji.postValue("Mesajlar yüklenemedi");
                        }
                    }

                    @Override
                    public void onFailure(Call<MesajListResponse> call, Throwable t) {
                        _hataMesaji.postValue("Hata: " + t.getMessage());
                    }
                });
    }

    public void mesajGonder(int gonderenId,
                            int aliciId,
                            String mesajText,
                            @Nullable String base64Image) {

        int resimVar = (base64Image == null || base64Image.isEmpty()) ? 0 : 1;

        api.mesajGonder(gonderenId, aliciId, mesajText, resimVar, base64Image)
                .enqueue(new Callback<SimpleResponse>() {
                    @Override
                    public void onResponse(Call<SimpleResponse> call,
                                           Response<SimpleResponse> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            yukleMesajlar(gonderenId, aliciId);
                        } else {
                            _hataMesaji.postValue("Mesaj gönderilemedi");
                        }
                    }

                    @Override
                    public void onFailure(Call<SimpleResponse> call, Throwable t) {
                        _hataMesaji.postValue("Hata: " + t.getMessage());
                    }
                });
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        if (mesajGuncelleRunnable != null) {
            handler.removeCallbacks(mesajGuncelleRunnable);
            mesajGuncelleRunnable = null;
        }
    }
}