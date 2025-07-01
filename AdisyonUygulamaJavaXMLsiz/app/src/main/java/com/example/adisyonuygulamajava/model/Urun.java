package com.example.adisyonuygulamajava.model;

public class Urun {

    private int id;
    private String urun_ad;
    private float urun_fiyat;
    private String urun_resim;
    private int urun_adet;
    private Kategori urunKategori;

    // Constructor
    public Urun(int id, String urun_ad, float urun_fiyat, String urun_resim, int urun_adet, Kategori urunKategori) {
        this.id = id;
        this.urun_ad = urun_ad;
        this.urun_fiyat = urun_fiyat;
        this.urun_resim = urun_resim;
        this.urun_adet = urun_adet;
        this.urunKategori = urunKategori;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getUrun_ad() {
        return urun_ad;
    }

    public void setUrun_ad(String urun_ad) {
        this.urun_ad = urun_ad;
    }

    public float getUrun_fiyat() {
        return urun_fiyat;
    }

    public void setUrun_fiyat(float urun_fiyat) {
        this.urun_fiyat = urun_fiyat;
    }

    public String getUrun_resim() {
        return urun_resim;
    }

    public void setUrun_resim(String urun_resim) {
        this.urun_resim = urun_resim;
    }

    public int getUrun_adet() {
        return urun_adet;
    }

    public void setUrun_adet(int urun_adet) {
        this.urun_adet = urun_adet;
    }

    public Kategori getUrunKategori() {
        return urunKategori;
    }

    public void setUrunKategori(Kategori urunKategori) {
        this.urunKategori = urunKategori;
    }
}

