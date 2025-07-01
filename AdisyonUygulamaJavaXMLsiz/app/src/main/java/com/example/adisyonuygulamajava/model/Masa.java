package com.example.adisyonuygulamajava.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Masa {

    private int id;
    private String masa_adi;

    @SerializedName("acik_mi")
    private int acikMi;

    private String sure;
    private float toplam_fiyat;

    private transient List<Urun> urunler = new ArrayList<>();

    // Constructor
    public Masa(int id, String masa_adi, int acikMi, String sure, float toplam_fiyat) {
        this.id = id;
        this.masa_adi = masa_adi;
        this.acikMi = acikMi;
        this.sure = sure;
        this.toplam_fiyat = toplam_fiyat;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getMasa_adi() {
        return masa_adi;
    }

    public int getAcikMi() {
        return acikMi;
    }

    public String getSure() {
        return sure;
    }

    public float getToplam_fiyat() {
        return toplam_fiyat;
    }

    public List<Urun> getUrunler() {
        return urunler;
    }

    public void setUrunler(List<Urun> urunler) {
        this.urunler = urunler;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMasa_adi(String masa_adi) {
        this.masa_adi = masa_adi;
    }

    public void setAcikMi(int acikMi) {
        this.acikMi = acikMi;
    }

    public void setSure(String sure) {
        this.sure = sure;
    }

    public void setToplam_fiyat(float toplam_fiyat) {
        this.toplam_fiyat = toplam_fiyat;
    }
}

