package com.example.adisyonuygulamajava.viewmodel;

public class UseCases {

    public static String fiyatYaz(float fiyat) {
        return String.format("%.2f ₺", fiyat);
    }
}

