package com.example.isletimsistemicalismasi.anaekran.model;

import android.content.Context;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.Serializable;

public class UygulamalarModel implements Serializable {
    String uygulamaAdi;
    String uygulamaResimAdi;
    int navId;
    boolean copKutusundaMi;

    public UygulamalarModel(String uygulamaAdi, String uygulamaResimAdi,int navId,boolean copKutusundaMi) {
        this.uygulamaAdi = uygulamaAdi;
        this.uygulamaResimAdi = uygulamaResimAdi;
        this.navId = navId;
        this.copKutusundaMi = copKutusundaMi;
    }

    public boolean isCopKutusundaMi() {
        return copKutusundaMi;
    }

    public int getNavId() {
        return navId;
    }

    public UygulamalarModel() {
    }

    public String getUygulamaAdi() {
        return uygulamaAdi;
    }

    public void setUygulamaAdi(String uygulamaAdi) {
        this.uygulamaAdi = uygulamaAdi;
    }

    public String getUygulamaResimAdi() {
        return uygulamaResimAdi;
    }

    public void setUygulamaResimAdi(String uygulamaResimAdi) {
        this.uygulamaResimAdi = uygulamaResimAdi;
    }
}
