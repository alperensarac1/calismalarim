package com.example.chatjava.model;

public class Mesaj {
    private int id;
    private int gonderen_id;
    private int alici_id;
    private String mesaj_text;
    private int resim_var;
    private String resim_url;
    private String tarih;

    public Mesaj(int id, int gonderen_id, int alici_id, String mesaj_text, int resim_var, String resim_url, String tarih) {
        this.id = id;
        this.gonderen_id = gonderen_id;
        this.alici_id = alici_id;
        this.mesaj_text = mesaj_text;
        this.resim_var = resim_var;
        this.resim_url = resim_url;
        this.tarih = tarih;
    }

    public int getId() {
        return id;
    }

    public int getGonderen_id() {
        return gonderen_id;
    }

    public int getAlici_id() {
        return alici_id;
    }

    public String getMesaj_text() {
        return mesaj_text;
    }

    public int getResim_var() {
        return resim_var;
    }

    public String getResim_url() {
        return resim_url;
    }

    public String getTarih() {
        return tarih;
    }
}

