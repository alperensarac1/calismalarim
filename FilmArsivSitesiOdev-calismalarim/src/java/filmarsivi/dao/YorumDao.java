/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.dao;

import filmarsivi.model.Yorum;
import filmarsivi.utils.BaseDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alperensarac
 */



public class YorumDao extends BaseDao {

   // Yorum ekleme
public boolean insert(Yorum yorum) {
    String sql = "INSERT INTO Yorumlar (kullanici_id, film_id, yorum_text, yorum_tarih) VALUES (?, ?, ?, ?)";
    
    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        
        // Parametreleri set et
        ps.setInt(1, yorum.getKullaniciId());
        ps.setInt(2, yorum.getFilmId());
        ps.setString(3, yorum.getYorumText());
        
        // Yorum tarihini formatla (tarih String olarak alınıyor)
        ps.setString(4, yorum.getYorumTarih()); // Tarihi olduğu gibi alıyoruz

        // Veritabanına ekle
        int rowsAffected = ps.executeUpdate();
        
        // Başarı durumuna göre true veya false döndür
        return rowsAffected > 0;
    } catch (Exception e) { 
        // Hata mesajını logla
        // Hata mesajını logla
        return false;  // Hata durumunda false döndür
    }
}



// Film ID'ye göre yorumları listele (kullanıcı adı ve tarih dahil)
public List<Yorum> listByFilmId(int filmId) {
    List<Yorum> yorumList = new ArrayList<>();

    String sql = "SELECT y.id, y.yorum_text, y.yorum_tarih, k.kullanici_adi, y.film_id, y.kullanici_id " +
                 "FROM Yorumlar y " +
                 "INNER JOIN Kullanicilar k ON y.kullanici_id = k.id " +
                 "WHERE y.film_id = ? " +
                 "ORDER BY y.id DESC";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, filmId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Yorum yorum = new Yorum(); // ✅ HER SATIR İÇİN YENİ NESNE

                yorum.setId(rs.getInt("id"));
                yorum.setFilmId(rs.getInt("film_id"));
                yorum.setKullaniciId(rs.getInt("kullanici_id"));
                yorum.setYorumText(rs.getString("yorum_text"));
                yorum.setYorumTarih(rs.getString("yorum_tarih")); // Veritabanındaki tarih
                yorum.setKullaniciAdi(rs.getString("kullanici_adi"));

                yorumList.add(yorum); // Listeye ekle
            }
        }

    } catch (Exception e) {
        // Hataları görmek için
        
    }

    return yorumList;
}



    // Yorum silme
    public boolean delete(int yorumId) {
        String sql = "DELETE FROM Yorumlar WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, yorumId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
        }

        return false;
    }
    public Yorum findById(int yorumId) {
        Yorum yorum = null;

        String sql = "SELECT y.id, y.kullanici_id, y.film_id, y.yorum_text, y.yorum_tarih " +
                     "FROM Yorumlar y WHERE y.id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, yorumId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    yorum = new Yorum();
                    yorum.setId(rs.getInt("id"));
                    yorum.setKullaniciId(rs.getInt("kullanici_id"));
                    yorum.setFilmId(rs.getInt("film_id"));
                    yorum.setYorumText(rs.getString("yorum_text"));
                    yorum.setYorumTarih(rs.getString("yorum_tarih"));
                }
            }

        } catch (Exception e) {
        }
        
        return yorum;
       
    }
}