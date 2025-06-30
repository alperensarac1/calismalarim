package com.example.isletimsistemicalismasi.uygulamalar.notdefteri.model;

import java.io.Serializable;

public class Not implements Serializable {
    int id;
    String baslik;
    String notMetin;
    String listName;
    String imageUrl;
    String tarih;
    String renk;

    public Not(int id,String baslik, String notMetin, String listName, String imageUrl, String tarih, String renk) {
        this.id = id;
        this.baslik = baslik;
        this.notMetin = notMetin;
        this.listName = listName;
        this.imageUrl = imageUrl;
        this.tarih = tarih;
        this.renk = renk;
    }

    public Not() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBaslik() {
        return baslik;
    }

    public void setBaslik(String baslik) {
        this.baslik = baslik;
    }

    public String getNotMetin() {
        return notMetin;
    }

    public void setNotMetin(String notMetin) {
        this.notMetin = notMetin;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTarih() {
        return tarih;
    }

    public void setTarih(String tarih) {
        this.tarih = tarih;
    }

    public String getRenk() {
        return renk;
    }

    public void setRenk(String renk) {
        this.renk = renk;
    }
}
