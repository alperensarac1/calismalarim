/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.dao;

/**
 *
 * @author alperensarac
 */


import filmarsivi.model.YorumBegeni;
import filmarsivi.utils.BaseDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class YorumBegeniDao extends BaseDao {

    // 1. Yorum beğenisi ekle
    public boolean insert(YorumBegeni begeni) {
        String sql = "INSERT INTO yorum_begeni (yorum_id, kullanici_id) VALUES (?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, begeni.getYorumId());
            ps.setInt(2, begeni.getKullaniciId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
        }

        return false;
    }
public boolean hasUserLikedComment(int kullaniciId, int yorumId) {
    String sql = "SELECT COUNT(*) FROM yorum_begeni WHERE kullanici_id = ? AND yorum_id = ?";
    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, kullaniciId);
        ps.setInt(2, yorumId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
    } catch (Exception e) {
    }
    return false;
}

    // 2. Yorum beğenisini sil (yorumId ve kullaniciId ile)
    public boolean delete(int yorumId, int kullaniciId) {
        String sql = "DELETE FROM yorum_begeni WHERE yorum_id = ? AND kullanici_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, yorumId);
            ps.setInt(2, kullaniciId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
        }

        return false;
    }

    // 3. Belirli yorum için toplam beğeni sayısı
    public int countByYorumId(int yorumId) {
        String sql = "SELECT COUNT(*) FROM yorum_begeni WHERE yorum_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, yorumId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
        }

        return 0;
    }

    // 4. Belirli kullanıcı bir yorumu beğenmiş mi kontrolü
    public boolean isLiked(int yorumId, int kullaniciId) {
        String sql = "SELECT COUNT(*) FROM yorum_begeni WHERE yorum_id = ? AND kullanici_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, yorumId);
            ps.setInt(2, kullaniciId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
        }

        return false;
    }

    // 5. Belirli yorumun tüm beğenilerini listele
    public List<YorumBegeni> listByYorumId(int yorumId) {
        List<YorumBegeni> liste = new ArrayList<>();
        String sql = "SELECT * FROM yorum_begeni WHERE yorum_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, yorumId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                YorumBegeni b = new YorumBegeni();
                b.setId(rs.getInt("id"));
                b.setYorumId(rs.getInt("yorum_id"));
                b.setKullaniciId(rs.getInt("kullanici_id"));
                liste.add(b);
            }

        } catch (Exception e) {
        }

        return liste;
    }
}

