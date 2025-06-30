/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.model;

/**
 *
 * @author alperensarac
 */


public class Favori {
    private int id;
    private int filmId;
    private int kullaniciId;

    // JOIN işlemleri için isteğe bağlı alanlar
    private String filmBaslik;
    private String kullaniciAdi;

    // Boş constructor
    public Favori() {}

    // Ana constructor
    public Favori(int id, int filmId, int kullaniciId) {
        this.id = id;
        this.filmId = filmId;
        this.kullaniciId = kullaniciId;
    }

    // Getter ve Setter'lar
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFilmId() {
        return filmId;
    }

    public void setFilmId(int filmId) {
        this.filmId = filmId;
    }

    public int getKullaniciId() {
        return kullaniciId;
    }

    public void setKullaniciId(int kullaniciId) {
        this.kullaniciId = kullaniciId;
    }

    public String getFilmBaslik() {
        return filmBaslik;
    }

    public void setFilmBaslik(String filmBaslik) {
        this.filmBaslik = filmBaslik;
    }

    public String getKullaniciAdi() {
        return kullaniciAdi;
    }

    public void setKullaniciAdi(String kullaniciAdi) {
        this.kullaniciAdi = kullaniciAdi;
    }
}

