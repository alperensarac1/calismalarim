package com.example.qrkodolusturucujava.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Urun implements Serializable {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("urun_ad")
    @Expose
    private String urunAd;

    @SerializedName("urun_resim")
    @Expose
    private String urunResim;

    @SerializedName("urun_aciklama")
    @Expose
    private String urunAciklama;

    @SerializedName("urun_kategori_id")
    @Expose
    private int urunKategoriId;

    @SerializedName("urun_indirim")
    @Expose
    private int urunIndirim;

    @SerializedName("urun_fiyat")
    @Expose
    private double urunFiyat;

    @SerializedName("urun_indirimli_fiyat")
    @Expose
    private double urunIndirimliFiyat;


    public Urun(int id, String urunAd, String urunResim, String urunAciklama, int urunKategoriId, int urunIndirim, double urunFiyat, double urunIndirimliFiyat) {
        this.id = id;
        this.urunAd = urunAd;
        this.urunResim = urunResim;
        this.urunAciklama = urunAciklama;
        this.urunKategoriId = urunKategoriId;
        this.urunIndirim = urunIndirim;
        this.urunFiyat = urunFiyat;
        this.urunIndirimliFiyat = urunIndirimliFiyat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrunAd() {
        return urunAd;
    }

    public void setUrunAd(String urunAd) {
        this.urunAd = urunAd;
    }

    public String getUrunResim() {
        return urunResim;
    }

    public void setUrunResim(String urunResim) {
        this.urunResim = urunResim;
    }

    public String getUrunAciklama() {
        return urunAciklama;
    }

    public void setUrunAciklama(String urunAciklama) {
        this.urunAciklama = urunAciklama;
    }

    public int getUrunKategoriId() {
        return urunKategoriId;
    }

    public void setUrunKategoriId(int urunKategoriId) {
        this.urunKategoriId = urunKategoriId;
    }

    public int getUrunIndirim() {
        return urunIndirim;
    }

    public void setUrunIndirim(int urunIndirim) {
        this.urunIndirim = urunIndirim;
    }

    public double getUrunFiyat() {
        return urunFiyat;
    }

    public void setUrunFiyat(double urunFiyat) {
        this.urunFiyat = urunFiyat;
    }

    public double getUrunIndirimliFiyat() {
        return urunIndirimliFiyat;
    }

    public void setUrunIndirimliFiyat(double urunIndirimliFiyat) {
        this.urunIndirimliFiyat = urunIndirimliFiyat;
    }
}

