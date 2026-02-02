package com.example.kargopaylasimjava.service;

import android.content.Context;
import android.util.Log;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private ApiClient() {}

    private static final String BASE_URL = "https://alperensaracdeneme.com/cargo/";
    private static final String HEADER_TOKEN = "X-Auth-Token";

    public static CargoApi createApi(Context context) {
        TokenStore tokenStore = new TokenStore(context);

        Interceptor authInterceptor = chain -> {
            String token = tokenStore.getToken();
            okhttp3.Request req = chain.request();

            if (token != null && !token.trim().isEmpty()) {
                Log.d("AUTH_TOKEN", token.substring(0, Math.min(12, token.length())) + "...");
            } else {
                Log.d("AUTH_TOKEN", "NULL/EMPTY");
            }

            okhttp3.Request newReq;
            if (token != null && !token.trim().isEmpty()) {
                newReq = req.newBuilder()
                        .removeHeader("Authorization")
                        .removeHeader(HEADER_TOKEN)
                        .addHeader(HEADER_TOKEN, token)
                        .build();
            } else {
                newReq = req;
            }

            return chain.proceed(newReq);
        };

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor urlLogger = chain -> {
            okhttp3.Request req = chain.request();
            Log.d("HTTP_URL", req.method() + " " + req.url());
            return chain.proceed(req);
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(urlLogger)
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(CargoApi.class);
    }
}

