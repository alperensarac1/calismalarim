package com.example.haberuygulamajava.model;

import java.io.Serializable;

public class HaberTuruModel implements Serializable {
    private int id;
    private String tur_adi;

    public HaberTuruModel() {}

    public HaberTuruModel(int id, String tur_adi) {
        this.id = id;
        this.tur_adi = tur_adi;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTur_adi() {
        return tur_adi;
    }

    public void setTur_adi(String tur_adi) {
        this.tur_adi = tur_adi;
    }

    @Override
    public String toString() {
        return tur_adi;
    }
}

