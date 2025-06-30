/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.model;

/**
 *
 * @author alperensarac
 */

public class Kullanici {
    private int id;
    private String kullaniciAdi;
    private String kullaniciSifre;
    private String kullaniciKayitTarihi; // <-- Burayı String yaptım

    // Boş constructor
    public Kullanici() {}

    // Tam constructor
    public Kullanici(int id, String kullaniciAdi, String kullaniciSifre, String kullaniciKayitTarihi) {
        this.id = id;
        this.kullaniciAdi = kullaniciAdi;
        this.kullaniciSifre = kullaniciSifre;
        this.kullaniciKayitTarihi = kullaniciKayitTarihi;
    }

    // Getter ve Setter'lar

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKullaniciAdi() {
        return kullaniciAdi;
    }

    public void setKullaniciAdi(String kullaniciAdi) {
        this.kullaniciAdi = kullaniciAdi;
    }

    public String getKullaniciSifre() {
        return kullaniciSifre;
    }

    public void setKullaniciSifre(String kullaniciSifre) {
        this.kullaniciSifre = kullaniciSifre;
    }

    public String getKullaniciKayitTarihi() {
        return kullaniciKayitTarihi;
    }

    public void setKullaniciKayitTarihi(String kullaniciKayitTarihi) {
        this.kullaniciKayitTarihi = kullaniciKayitTarihi;
    }
}
