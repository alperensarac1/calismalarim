package com.example.dosyapaylasimjava.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://alperensaracdeneme.com/api/";
    private static ApiService api;

    public static ApiService getApi() {
        if (api == null) {
            synchronized (RetrofitClient.class) {
                if (api == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    api = retrofit.create(ApiService.class);
                }
            }
        }
        return api;
    }

    private RetrofitClient() {}
}

