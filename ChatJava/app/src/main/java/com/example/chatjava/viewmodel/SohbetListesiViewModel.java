package com.example.chatjava.viewmodel;



import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatjava.model.KonusulanKisi;
import com.example.chatjava.service.ApiService;
import com.example.chatjava.service.RetrofitClient;
import com.example.chatjava.service.response.KonusulanKisiListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SohbetListesiViewModel extends ViewModel {


    private final MutableLiveData<List<KonusulanKisi>> _konusulanKisiler = new MutableLiveData<>();
    public LiveData<List<KonusulanKisi>> getKonusulanKisiler() { return _konusulanKisiler; }

    private final MutableLiveData<String> _hataMesaji = new MutableLiveData<>();
    public LiveData<String> getHataMesaji() { return _hataMesaji; }


    private final ApiService api = RetrofitClient.getApiService();


    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable yenilemeRunnable;

    public void sohbetListesiniBaslat(int kullaniciId) {
        // Önceki döngü varsa iptal et
        if (yenilemeRunnable != null) {
            handler.removeCallbacks(yenilemeRunnable);
        }

        yenilemeRunnable = new Runnable() {
            @Override
            public void run() {
                sohbetListesiniGetir(kullaniciId);
                handler.postDelayed(this, 15_000);
            }
        };
        handler.post(yenilemeRunnable);
    }


    private void sohbetListesiniGetir(int kullaniciId) {
        api.konusulanKisiler(kullaniciId)
                .enqueue(new Callback<KonusulanKisiListResponse>() {
                    @Override
                    public void onResponse(Call<KonusulanKisiListResponse> call,
                                           Response<KonusulanKisiListResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            _konusulanKisiler.postValue(response.body().getKisiler());
                            _hataMesaji.postValue(null);

                        } else {
                            _hataMesaji.postValue("Liste alınamadı");
                        }
                    }

                    @Override
                    public void onFailure(Call<KonusulanKisiListResponse> call, Throwable t) {
                        _hataMesaji.postValue("Sunucu hatası: " + t.getMessage());
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (yenilemeRunnable != null) {
            handler.removeCallbacks(yenilemeRunnable);
            yenilemeRunnable = null;
        }
    }
}

