package com.example.kargopaylasimjava.di;

import android.content.Context;

import com.example.kargopaylasimjava.repo.CargoRepository;
import com.example.kargopaylasimjava.service.ApiClient;
import com.example.kargopaylasimjava.service.CargoApi;
import com.example.kargopaylasimjava.service.TokenStore;

public class AppContainer {
    public final TokenStore tokenStore;
    public final CargoApi api;
    public final CargoRepository repo;

    public AppContainer(Context context) {
        tokenStore = new TokenStore(context);
        api = ApiClient.createApi(context);
        repo = new CargoRepository(api, tokenStore);
    }
}

