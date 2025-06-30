package com.example.qrkodolusturucujava.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UrunCevap {

    @SerializedName("kahve_urun")
    @Expose
    private List<Urun> kahveUrun;

    public UrunCevap(List<Urun> kahveUrun) {
        this.kahveUrun = kahveUrun;
    }

    public List<Urun> getKahveUrun() {
        return kahveUrun;
    }

    public void setKahveUrun(List<Urun> kahveUrun) {
        this.kahveUrun = kahveUrun;
    }
}

