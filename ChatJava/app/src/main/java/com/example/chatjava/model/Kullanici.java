package com.example.chatjava.model;

public class Kullanici {
    private int id;
    private String ad;
    private String numara;

    public Kullanici(int id, String ad, String numara) {
        this.id = id;
        this.ad = ad;
        this.numara = numara;
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
}

