package com.example.adisyonuygulamajava.model;

public class Kategori {
    int id;
    String kategori_ad;

    public Kategori(int id, String kategori_ad) {
        this.id = id;
        this.kategori_ad = kategori_ad;
    }

    public Kategori() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKategori_ad() {
        return kategori_ad;
    }

    public void setKategori_ad(String kategori_ad) {
        this.kategori_ad = kategori_ad;
    }
}
