package com.example.haberuygulamajava.model;

import java.io.Serializable;

public class YorumInsertRequest implements Serializable {
    private int haber_id;
    private String takma_ad;
    private String yorum_metni;

    public YorumInsertRequest() {}

    public YorumInsertRequest(int haber_id, String takma_ad, String yorum_metni) {
        this.haber_id = haber_id;
        this.takma_ad = takma_ad;
        this.yorum_metni = yorum_metni;
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
}

