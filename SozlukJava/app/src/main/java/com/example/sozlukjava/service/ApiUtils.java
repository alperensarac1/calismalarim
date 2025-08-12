package com.example.sozlukjava.service;

// ApiUtils.java
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiUtils {
    private static final String BASE_URL = "https://alperensaracdeneme.com/sozluk/";
    private static Retrofit retrofit;

    private ApiUtils() {}

    private static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static SozlukApiService getService() {
        return getClient().create(SozlukApiService.class);
    }
}

