package com.example.qrkodolusturucujava.servicesTest;

import com.example.qrkodolusturucujava.model.UrunCevap;
import com.example.qrkodolusturucujava.services.dummyservice.KahveDummyServisDao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KahveDummyServiceTest {

    private KahveDummyServisDao kahveDummyServisDao;
    private UrunCevap urunCevap;

    @Before
    public void setup() {
        kahveDummyServisDao = new KahveDummyServisDao();
    }

    @Test
    public void tumKahvelerGetirTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        kahveDummyServisDao.tumKahveler(new Callback<UrunCevap>() {
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


