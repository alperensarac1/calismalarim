package com.example.eticaretjava.repo;

import com.example.eticaretjava.service.AuthApi;
import com.example.eticaretjava.service.ApiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitProvider {

    private static final String BASE_URL = "https://alperensaracdeneme.com/eticaret/api/"; // sondaki / şart

    private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

    static {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build();

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static final ApiService.ProductApi productApi = retrofit.create(ApiService.ProductApi.class);
    public static final AuthApi authApi = retrofit.create(AuthApi.class);
    public static final ApiService.CartApi cartApi = retrofit.create(ApiService.CartApi.class);
    public static final ApiService.OrderApi orderApi = retrofit.create(ApiService.OrderApi.class);
}

