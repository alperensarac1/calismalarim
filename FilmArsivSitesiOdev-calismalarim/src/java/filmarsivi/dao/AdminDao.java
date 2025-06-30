/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.dao;

import filmarsivi.utils.BaseDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;



public class AdminDao extends BaseDao {

    public boolean isAdmin(int kullaniciId) {
        String sql = "SELECT * FROM Adminler WHERE kullanici_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, kullaniciId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); 
            }

        } catch (Exception e) {
        }

        return false;
    }
    public boolean insert(int kullaniciId) {
    String sql = "INSERT INTO Adminler (kullanici_id) VALUES (?)";
    try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, kullaniciId);
        return ps.executeUpdate() > 0;
    } catch (Exception e) {
        return false; 
    }
}

}


