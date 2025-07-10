package com.example.chatjava.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://alperensaracdeneme.com/mesajlasma/";
    private static ApiService apiService;

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService.class);
        }
        return apiService;
    }
}

