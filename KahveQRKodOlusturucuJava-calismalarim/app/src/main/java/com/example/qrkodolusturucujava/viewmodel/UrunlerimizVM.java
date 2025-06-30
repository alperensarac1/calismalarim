package com.example.qrkodolusturucujava.viewmodel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodolusturucujava.adapter.UrunAdapter;
import com.example.qrkodolusturucujava.model.Urun;
import com.example.qrkodolusturucujava.model.UrunCevap;
import com.example.qrkodolusturucujava.model.UrunKategori;
import com.example.qrkodolusturucujava.services.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class UrunlerimizVM extends ViewModel {

    private static final String TAG = "UrunlerimizVM";
    private final Services kahveServisDao;

    @Inject
    public UrunlerimizVM(Services kahveServisDao) {
        this.kahveServisDao = kahveServisDao;
    }

    public void recyclerviewAyarla(UrunKategori urunKategori, RecyclerView recyclerView, Context context) {
        kahveServisDao.kahveWithKategoriId(urunKategori.kategoriKodu(), new Callback<UrunCevap>() {
            @Override
            public void onResponse(Call<UrunCevap> call, Response<UrunCevap> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Urun> urunler = response.body().getKahveUrun();

                    UrunAdapter adapter = new UrunAdapter(urunler);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "Alınan ürünler: " + urunler.toString());
                } else {
                    Log.e(TAG, "Sunucu hatası: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UrunCevap> call, Throwable t) {
                Log.e(TAG, "Veri çekme hatası: " + t.getMessage());
            }
        });
    }

    public void indirimliRecyclerAyarla(RecyclerView recyclerView, Context context) {
        kahveServisDao.tumKahveler(new Callback<UrunCevap>() {
            @Override
            public void onResponse(Call<UrunCevap> call, Response<UrunCevap> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Urun> tumUrunler = response.body().getKahveUrun();

                    List<Urun> indirimliUrunler = new ArrayList<>();
                    for (Urun urun : tumUrunler) {
                        if (urun.getUrunIndirim() == 1) {
                            indirimliUrunler.add(urun);
                        }
                    }

                    UrunAdapter adapter = new UrunAdapter(indirimliUrunler);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "İndirimli ürünler yüklendi.");
                } else {
                    Log.e(TAG, "Sunucu hatası: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UrunCevap> call, Throwable t) {
                Log.e(TAG, "Veri çekme hatası: " + t.getMessage());
            }
        });
    }
}
