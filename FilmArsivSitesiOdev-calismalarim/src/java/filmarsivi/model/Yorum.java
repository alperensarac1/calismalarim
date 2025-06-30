/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.model;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 *
 * @author alperensarac
 */




public class Yorum {
    private int id;
    private int kullaniciId;
    private int filmId;
    private String yorumText;
    private String yorumTarih; // yorum tarihi eklendi
    private String kullaniciAdi; // kullanıcı adı eklendi

    public Yorum() {}

    public Yorum(int id, int kullaniciId, int filmId, String yorumText, String yorumTarih) {
        this.id = id;
        this.kullaniciId = kullaniciId;
        this.filmId = filmId;
        this.yorumText = yorumText;
        this.yorumTarih = yorumTarih;
    }

    // Getter-Setter'lar

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getKullaniciId() { return kullaniciId; }
    public void setKullaniciId(int kullaniciId) { this.kullaniciId = kullaniciId; }

    public int getFilmId() { return filmId; }
    public void setFilmId(int filmId) { this.filmId = filmId; }

    public String getYorumText() { return yorumText; }
    public void setYorumText(String yorumText) { this.yorumText = yorumText; }

    public void setYorumTarih(String yorumTarih) {
        this.yorumTarih = yorumTarih;
    }

    public String getYorumTarih() {
        return yorumTarih;
    }

    public String getKullaniciAdi() { return kullaniciAdi; }
    public void setKullaniciAdi(String kullaniciAdi) { this.kullaniciAdi = kullaniciAdi; }
}

