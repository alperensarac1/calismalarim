package com.example.chatjava.model;

public class KonusulanKisi {
    private int id;
    private String ad;
    private String numara;
    private String son_mesaj;
    private String tarih;

    public KonusulanKisi(int id, String ad, String numara, String son_mesaj, String tarih) {
        this.id = id;
        this.ad = ad;
        this.numara = numara;
        this.son_mesaj = son_mesaj;
        this.tarih = tarih;
    }

    public int getId() {
        return id;
    }

    public String getAd() {
        return ad;
    }

    public String getNumara() {
        return numara;
    }

    public String getSon_mesaj() {
        return son_mesaj;
    }

    public String getTarih() {
        return tarih;
    }
}

