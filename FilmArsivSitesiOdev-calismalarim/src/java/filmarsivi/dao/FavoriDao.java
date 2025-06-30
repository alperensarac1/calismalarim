/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.dao;

import filmarsivi.model.Favori;
import filmarsivi.utils.BaseDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;



public class FavoriDao extends BaseDao {

    public boolean insert(Favori favori) {
    String sql = "INSERT INTO Favoriler (film_id, kullanici_id) VALUES (?, ?)";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, favori.getFilmId());
        ps.setInt(2, favori.getKullaniciId());

        int rowsAffected = ps.executeUpdate();
        return rowsAffected > 0;

    } catch (Exception e) {
        // Hata mesajını görmek için
        
    }

    return false;
}
public boolean delete(int kullaniciId, int filmId) {
    String sql = "DELETE FROM Favoriler WHERE kullanici_id = ? AND film_id = ?";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, kullaniciId);
        ps.setInt(2, filmId);

        int rowsAffected = ps.executeUpdate();
        return rowsAffected > 0;

    } catch (Exception e) {
        // Hata mesajını görmek için
        
    }

    return false;
}


    // 2. Belirli kullanıcıya ait favori filmleri getir
    public List<Favori> listByKullaniciId(int kullaniciId) {
        List<Favori> favoriList = new ArrayList<>();
        String sql = "SELECT f.*, fl.film_baslik FROM Favoriler f " +
                     "INNER JOIN Filmler fl ON f.film_id = fl.id WHERE f.kullanici_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, kullaniciId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Favori favori = new Favori();
                    favori.setId(rs.getInt("id"));
                    favori.setFilmId(rs.getInt("film_id"));
                    favori.setKullaniciId(rs.getInt("kullanici_id"));
                    favori.setFilmBaslik(rs.getString("film_baslik"));
                    favoriList.add(favori);
                }
            }

        } catch (Exception e) {
        }

        return favoriList;
    }

    // 3. Belirli filmin kaç kez favorilere eklendiğini döndür
    public int getFavoriSayisi(int filmId) {
        String sql = "SELECT COUNT(*) FROM Favoriler WHERE film_id = ?";
        int sayi = 0;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, filmId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sayi = rs.getInt(1);
                }
            }

        } catch (Exception e) {
        }

        return sayi;
    }

  

   public boolean isFavorited(int kullaniciId, int filmId) {
    String sql = "SELECT * FROM Favoriler WHERE kullanici_id = ? AND film_id = ?";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        // Parametreleri ayarlıyoruz
        ps.setInt(1, kullaniciId);
        ps.setInt(2, filmId);

        // Sorguyu çalıştırıyoruz
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next(); // Eğer sonuç varsa true döner (favoriye eklenmiş)
        }

    } catch (Exception e) { // Hata detayını yazdırıyoruz
        // Hata detayını yazdırıyoruz
        return false; // Hata durumunda false döner
    }
}

}

