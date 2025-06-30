package com.example.qrkodolusturucujava.servicesTest;

import com.example.qrkodolusturucujava.model.UrunCevap;
import com.example.qrkodolusturucujava.services.retrofit.KahveHTTPServisDao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KahveHTTPServiceTest {

    private KahveHTTPServisDao kahveHTTPServisDao;
    private UrunCevap urunCevap;

    @Before
    public void setup() {
        kahveHTTPServisDao = new KahveHTTPServisDao(); // Gerçek HTTP servisi ise Retrofit ile ayarlı olmalı
    }

    @Test
    public void tumKahveleriGetirTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        kahveHTTPServisDao.tumKahveler(new Callback<UrunCevap>() {
            @Override
            public void onResponse(Call<UrunCevap> call, Response<UrunCevap> response) {
                urunCevap = response.body();
                latch.countDown();
            }

            @Override
            public void onFailure(Call<UrunCevap> call, Throwable t) {
                latch.countDown();
            }
        });

        latch.await(); // Testin bitmesini bekler
        Assert.assertNotNull(urunCevap);
        Assert.assertNotNull(urunCevap.getKahveUrun());
    }
}

