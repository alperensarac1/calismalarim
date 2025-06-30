/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.model;

/**
 *
 * @author alperensarac
 */


public class Film {
    private int id;
    private String filmBaslik;
    private String filmCikisYili;
    private String filmSure;
    private String filmAciklama;
    private int filmKategoriId;
    private String filmResimUrl;
    private double filmPuan;
    private String filmUrl; // ✅ Yeni alan: video bağlantısı (örneğin YouTube URL)

    // Boş constructor
    public Film() {}

    // Tam constructor
    public Film(int id, String filmBaslik, String filmCikisYili, String filmSure, String filmAciklama,
                int filmKategoriId, String filmResimUrl, double filmPuan, String filmUrl) {
        this.id = id;
        this.filmBaslik = filmBaslik;
        this.filmCikisYili = filmCikisYili;
        this.filmSure = filmSure;
        this.filmAciklama = filmAciklama;
        this.filmKategoriId = filmKategoriId;
        this.filmResimUrl = filmResimUrl;
        this.filmPuan = filmPuan;
        this.filmUrl = filmUrl;
    }

    // Getter - Setter'lar
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilmBaslik() {
        return filmBaslik;
    }

    public void setFilmBaslik(String filmBaslik) {
        this.filmBaslik = filmBaslik;
    }

    public String getFilmCikisYili() {
        return filmCikisYili;
    }

    public void setFilmCikisYili(String filmCikisYili) {
        this.filmCikisYili = filmCikisYili;
    }

    public String getFilmSure() {
        return filmSure;
    }

    public void setFilmSure(String filmSure) {
        this.filmSure = filmSure;
    }

    public String getFilmAciklama() {
        return filmAciklama;
    }

    public void setFilmAciklama(String filmAciklama) {
        this.filmAciklama = filmAciklama;
    }

    public int getFilmKategoriId() {
        return filmKategoriId;
    }

    public void setFilmKategoriId(int filmKategoriId) {
        this.filmKategoriId = filmKategoriId;
    }

    public String getFilmResimUrl() {
        return filmResimUrl;
    }

    public void setFilmResimUrl(String filmResimUrl) {
        this.filmResimUrl = filmResimUrl;
    }

    public double getFilmPuan() {
        return filmPuan;
    }

    public void setFilmPuan(double filmPuan) {
        this.filmPuan = filmPuan;
    }

    public String getFilmUrl() {
        return filmUrl;
    }

    public void setFilmUrl(String filmUrl) {
        this.filmUrl = filmUrl;
    }
}
