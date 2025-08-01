package com.example.haberuygulamajava.model;

import java.io.Serializable;

public class YorumModel implements Serializable {
    private int id;
    private int haber_id;
    private String takma_ad;
    private String yorum_metni;
    private int onayli;
    private String yorum_tarihi;

    public YorumModel() {}

    public YorumModel(int id, int haber_id, String takma_ad, String yorum_metni, int onayli, String yorum_tarihi) {
        this.id = id;
        this.haber_id = haber_id;
        this.takma_ad = takma_ad;
        this.yorum_metni = yorum_metni;
        this.onayli = onayli;
        this.yorum_tarihi = yorum_tarihi;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHaber_id() {
        return haber_id;
    }

    public void setHaber_id(int haber_id) {
        this.haber_id = haber_id;
    }

    public String getTakma_ad() {
        return takma_ad;
    }

    public void setTakma_ad(String takma_ad) {
        this.takma_ad = takma_ad;
    }

    public String getYorum_metni() {
        return yorum_metni;
    }

    public void setYorum_metni(String yorum_metni) {
        this.yorum_metni = yorum_metni;
    }

    public int getOnayli() {
        return onayli;
    }

    public void setOnayli(int onayli) {
        this.onayli = onayli;
    }

    public String getYorum_tarihi() {
        return yorum_tarihi;
    }

    public void setYorum_tarihi(String yorum_tarihi) {
        this.yorum_tarihi = yorum_tarihi;
    }
}

