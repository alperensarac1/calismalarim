package com.example.adisyonuygulamajava.services.retrofit;

import com.example.adisyonuygulamajava.services.Services;

public class ServiceImpl {

    private static Services instance;

    private ServiceImpl() {
        // Private constructor to prevent instantiation
    }

    public static Services getInstance() {
        if (instance == null) {
            instance = new AdisyonServisDao();
        }
        return instance;
    }
}

