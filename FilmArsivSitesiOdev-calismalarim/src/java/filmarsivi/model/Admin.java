/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.model;

/**
 *
 * @author alperensarac
 */

public class Admin {
    private int id;
    private String adminKullaniciAdi;
    private String adminSifre;

    public Admin() {}

    public Admin(int id, String adminKullaniciAdi, String adminSifre) {
        this.id = id;
        this.adminKullaniciAdi = adminKullaniciAdi;
        this.adminSifre = adminSifre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAdminKullaniciAdi() {
        return adminKullaniciAdi;
    }

    public void setAdminKullaniciAdi(String adminKullaniciAdi) {
        this.adminKullaniciAdi = adminKullaniciAdi;
    }

    public String getAdminSifre() {
        return adminSifre;
    }

    public void setAdminSifre(String adminSifre) {
        this.adminSifre = adminSifre;
    }
}

