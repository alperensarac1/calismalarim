package com.example.adisyonuygulamajava.model;

public class MasaUrun {

    private int urun_id;
    private String urun_ad;
    private float birim_fiyat;
    private int adet;
    private float toplam_fiyat;

    // Constructor
    public MasaUrun(int urun_id, String urun_ad, float birim_fiyat, int adet, float toplam_fiyat) {
        this.urun_id = urun_id;
        this.urun_ad = urun_ad;
        this.birim_fiyat = birim_fiyat;
        this.adet = adet;
        this.toplam_fiyat = toplam_fiyat;
    }

    // Getters and Setters
    public int getUrun_id() {
        return urun_id;
    }

    public void setUrun_id(int urun_id) {
        this.urun_id = urun_id;
    }

    public String getUrun_ad() {
        return urun_ad;
    }

    public void setUrun_ad(String urun_ad) {
        this.urun_ad = urun_ad;
    }

    public float getBirim_fiyat() {
        return birim_fiyat;
    }

    public void setBirim_fiyat(float birim_fiyat) {
        this.birim_fiyat = birim_fiyat;
    }

    public int getAdet() {
        return adet;
    }

    public void setAdet(int adet) {
        this.adet = adet;
    }

    public float getToplam_fiyat() {
        return toplam_fiyat;
    }

    public void setToplam_fiyat(float toplam_fiyat) {
        this.toplam_fiyat = toplam_fiyat;
    }
}

