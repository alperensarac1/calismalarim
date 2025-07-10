package com.example.chatjava.service.response;

import com.example.chatjava.model.Kullanici;

import java.util.List;

public class KullaniciListResponse {
    private boolean success;
    private List<Kullanici> kullanicilar;

    public KullaniciListResponse(boolean success, List<Kullanici> kullanicilar) {
        this.success = success;
        this.kullanicilar = kullanicilar;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<Kullanici> getKullanicilar() {
        return kullanicilar;
    }
}

