package com.example.isletimsistemicalismasi.uygulamalar.rehber.model;

import java.io.Serializable;

public class RehberKisiler implements Serializable {
    int kisi_id;
    String kisi_isim;
    String kisi_numara;

    public RehberKisiler(int kisi_id, String kisi_isim, String kisi_numara) {
        this.kisi_id = kisi_id;
        this.kisi_isim = kisi_isim;
        this.kisi_numara = kisi_numara;
    }

    public RehberKisiler() {
    }

    public int getKisi_id() {
        return kisi_id;
    }

    public void setKisi_id(int kisi_id) {
        this.kisi_id = kisi_id;
    }

    public String getKisi_isim() {
        return kisi_isim;
    }

    public void setKisi_isim(String kisi_isim) {
        this.kisi_isim = kisi_isim;
    }

    public String getKisi_numara() {
        return kisi_numara;
    }

    public void setKisi_numara(String kisi_numara) {
        this.kisi_numara = kisi_numara;
    }
}
