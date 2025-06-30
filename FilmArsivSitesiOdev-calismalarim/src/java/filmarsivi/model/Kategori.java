/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.model;

/**
 *
 * @author alperensarac
 */
public class Kategori {
    int id;
    String kategori_ad;
    String kategori_resim; // Kategori için resim URL'si ekledik

    public Kategori(int id, String kategori_ad, String kategori_resim) {
        this.id = id;
        this.kategori_ad = kategori_ad;
        this.kategori_resim = kategori_resim; // Constructor'da resim URL'si de alınıyor
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

    public String getKategori_resim() {
        return kategori_resim; // Getter metodunu ekledik
    }

    public void setKategori_resim(String kategori_resim) {
        this.kategori_resim = kategori_resim; // Setter metodunu ekledik
    }
}

