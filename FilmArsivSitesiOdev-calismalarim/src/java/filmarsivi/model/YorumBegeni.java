/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.model;

/**
 *
 * @author alperensarac
 */


public class YorumBegeni {
    private int id;
    private int yorumId;
    private int kullaniciId;

    public YorumBegeni() {
    }

    public YorumBegeni(int id, int yorumId, int kullaniciId) {
        this.id = id;
        this.yorumId = yorumId;
        this.kullaniciId = kullaniciId;
    }

    // Getter ve Setter metodları
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getYorumId() {
        return yorumId;
    }

    public void setYorumId(int yorumId) {
        this.yorumId = yorumId;
    }

    public int getKullaniciId() {
        return kullaniciId;
    }

    public void setKullaniciId(int kullaniciId) {
        this.kullaniciId = kullaniciId;
    }
}

