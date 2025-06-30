/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.dao;

import filmarsivi.model.Kullanici;
import filmarsivi.utils.BaseDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;





public class KullaniciDao extends BaseDao {

    // 1. Yeni kullanıcı ekleme
    public boolean insert(Kullanici kullanici) {
        String sql = "INSERT INTO Kullanicilar (kullanici_adi, kullanici_sifre, kullanici_kayit_tarih) VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kullanici.getKullaniciAdi());
            ps.setString(2, kullanici.getKullaniciSifre());
            ps.setString(3, kullanici.getKullaniciKayitTarihi()); // Artık String olarak set ediliyor
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
        }

        return false;
    }

    // 2. Kullanıcı adına göre kullanıcıyı getir
    public Kullanici findByUsername(String kullaniciAdi) {
        String sql = "SELECT * FROM Kullanicilar WHERE kullanici_adi = ?";
        Kullanici kullanici = null;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kullaniciAdi);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kullanici = new Kullanici();
                    kullanici.setId(rs.getInt("id"));
                    kullanici.setKullaniciAdi(rs.getString("kullanici_adi"));
                    kullanici.setKullaniciSifre(rs.getString("kullanici_sifre"));
                    kullanici.setKullaniciKayitTarihi(rs.getString("kullanici_kayit_tarihi")); // Direkt String alıyoruz
                }
            }

        } catch (Exception e) {
        }

        return kullanici;
    }
 public boolean delete(int id) {
    String sql1 = "DELETE FROM yorum_begeni WHERE kullanici_id = ?";
    String sql2 = "DELETE FROM Favoriler WHERE kullanici_id = ?";
    String sql3 = "DELETE FROM Yorumlar WHERE kullanici_id = ?";
    String sql4 = "DELETE FROM Kullanicilar WHERE id = ?";

    try (Connection con = getConnection()) {
        con.setAutoCommit(false);

        try (
            PreparedStatement ps1 = con.prepareStatement(sql1);
            PreparedStatement ps2 = con.prepareStatement(sql2);
            PreparedStatement ps3 = con.prepareStatement(sql3);
            PreparedStatement ps4 = con.prepareStatement(sql4)
        ) {
            ps1.setInt(1, id);
            ps2.setInt(1, id);
            ps3.setInt(1, id);
            ps4.setInt(1, id);

            ps1.executeUpdate();
            ps2.executeUpdate();
            ps3.executeUpdate();
            int affected = ps4.executeUpdate();

            con.commit();
            return affected > 0;
        } catch (Exception e) {
            con.rollback();
        }

    } catch (Exception e) {
    }

    return false;
}


public List<Kullanici> searchByName(String adi) {
    List<Kullanici> liste = new ArrayList<>();
    String sql = "SELECT * FROM Kullanicilar WHERE kullanici_adi LIKE ?";
    try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, "%" + adi + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Kullanici k = new Kullanici();
            k.setId(rs.getInt("id"));
            k.setKullaniciAdi(rs.getString("kullanici_adi"));
            k.setKullaniciSifre(rs.getString("kullanici_sifre"));
            k.setKullaniciKayitTarihi(rs.getString("kullanici_kayit_tarih"));
            liste.add(k);
        }
    } catch (Exception e) {}
    return liste;
}


    // 3. Giriş kontrolü (kullanıcı adı + şifre)
    public boolean login(String kullaniciAdi, String sifre) {
        String sql = "SELECT * FROM Kullanicilar WHERE kullanici_adi = ? AND kullanici_sifre = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kullaniciAdi);
            ps.setString(2, sifre);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Eşleşme varsa true döner
            }

        } catch (Exception e) {
        }

        return false;
    }

    // 4. (Opsiyonel) Tüm kullanıcıları listele
    public List<Kullanici> listAll() {
        List<Kullanici> kullaniciList = new ArrayList<>();
        String sql = "SELECT * FROM Kullanicilar";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Kullanici k = new Kullanici();
                k.setId(rs.getInt("id"));
                k.setKullaniciAdi(rs.getString("kullanici_adi"));
                k.setKullaniciSifre(rs.getString("kullanici_sifre"));
                k.setKullaniciKayitTarihi(rs.getString("kullanici_kayit_tarihi")); // Burayı da String yaptık
                kullaniciList.add(k);
            }

        } catch (Exception e) {
        }

        return kullaniciList;
    }
}
