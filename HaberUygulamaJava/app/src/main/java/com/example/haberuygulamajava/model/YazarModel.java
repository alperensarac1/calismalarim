package com.example.haberuygulamajava.model;

import java.io.Serializable;

public class YazarModel implements Serializable {
    private int id;
    private String ad;
    private String soyad;
    private String unvan;

    public YazarModel() {}

    public YazarModel(int id, String ad, String soyad, String unvan) {
        this.id = id;
        this.ad = ad;
        this.soyad = soyad;
        this.unvan = unvan;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getSoyad() {
        return soyad;
    }

    public void setSoyad(String soyad) {
        this.soyad = soyad;
    }

    public String getUnvan() {
        return unvan;
    }

    public void setUnvan(String unvan) {
        this.unvan = unvan;
    }
}

