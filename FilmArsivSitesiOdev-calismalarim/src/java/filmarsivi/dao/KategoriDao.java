/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.dao;

import filmarsivi.model.Kategori;
import filmarsivi.utils.BaseDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;




public class KategoriDao extends BaseDao {

    public List<Kategori> listAll() {
        List<Kategori> kategoriList = new ArrayList<>();
        String sql = "SELECT * FROM Kategoriler ORDER BY kategori_ad ASC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Kategori kategori = new Kategori();
                kategori.setId(rs.getInt("id"));
                kategori.setKategori_ad(rs.getString("kategori_ad"));
                kategori.setKategori_resim(rs.getString("kategori_resim"));
                kategoriList.add(kategori);
            }

        } catch (Exception e) {
        }

        return kategoriList;
    }


    public boolean insert(Kategori kategori) {
        String sql = "INSERT INTO Kategoriler (kategori_ad, kategori_resim) VALUES (?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kategori.getKategori_ad());
            ps.setString(2, kategori.getKategori_resim()); 
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
        }

        return false;
    }

   
    public boolean update(Kategori kategori) {
        String sql = "UPDATE Kategoriler SET kategori_ad = ?, kategori_resim = ? WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kategori.getKategori_ad());
            ps.setString(2, kategori.getKategori_resim());
            ps.setInt(3, kategori.getId());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
        }

        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM Kategoriler WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
        }

        return false;
    }

    public Kategori findById(int id) {
        String sql = "SELECT * FROM Kategoriler WHERE id = ?";
        Kategori kategori = null;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kategori = new Kategori();
                    kategori.setId(rs.getInt("id"));
                    kategori.setKategori_ad(rs.getString("kategori_ad"));
                    kategori.setKategori_resim(rs.getString("kategori_resim")); // kategori resmi ekleniyor
                }
            }

        } catch (Exception e) {
        }

        return kategori;
    }
}
