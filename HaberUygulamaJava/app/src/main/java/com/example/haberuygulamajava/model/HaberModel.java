package com.example.haberuygulamajava.model;

import java.io.Serializable;

public class HaberModel implements Serializable {
    private int id;
    private String baslik;
    private String icerik;
    private String media_type;
    private String media_url;
    private String yayinlanma_tarihi;
    private int sondakika;
    private Integer yazar_id;
    private Integer tur_id;
    private String ad;       // yazar adı
    private String soyad;    // yazar soyadı
    private String unvan;    // yazar unvanı
    private String tur_adi;  // tür adı

    // Boş constructor (gerekirse Retrofit/Gson için)
    public HaberModel() {}

    // Getter ve Setter'lar
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBaslik() { return baslik; }
    public void setBaslik(String baslik) { this.baslik = baslik; }

    public String getIcerik() { return icerik; }
    public void setIcerik(String icerik) { this.icerik = icerik; }

    public String getMedia_type() { return media_type; }
    public void setMedia_type(String media_type) { this.media_type = media_type; }

    public String getMedia_url() { return media_url; }
    public void setMedia_url(String media_url) { this.media_url = media_url; }

    public String getYayinlanma_tarihi() { return yayinlanma_tarihi; }
    public void setYayinlanma_tarihi(String yayinlanma_tarihi) { this.yayinlanma_tarihi = yayinlanma_tarihi; }

    public int getSondakika() { return sondakika; }
    public void setSondakika(int sondakika) { this.sondakika = sondakika; }

    public Integer getYazar_id() { return yazar_id; }
    public void setYazar_id(Integer yazar_id) { this.yazar_id = yazar_id; }

    public Integer getTur_id() { return tur_id; }
    public void setTur_id(Integer tur_id) { this.tur_id = tur_id; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getUnvan() { return unvan; }
    public void setUnvan(String unvan) { this.unvan = unvan; }

    public String getTur_adi() { return tur_adi; }
    public void setTur_adi(String tur_adi) { this.tur_adi = tur_adi; }
}

