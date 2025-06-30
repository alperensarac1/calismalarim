/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filmarsivi.utils;

/**
 *
 * @author alperensarac
 */


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseDao {

    // Bağlantı alma işlemi, dışarıya Exception fırlatabilir
    protected Connection getConnection() throws Exception {
        return DatabaseUtilities.getConnection();
    }

    // Sadece PreparedStatement ve ResultSet kapatma
    protected void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
        }

        try {
            if (ps != null) ps.close();
        } catch (SQLException e) {
        }
    }

    // Connection ile birlikte kapatma
    protected void close(Connection con, PreparedStatement ps, ResultSet rs) {
        close(ps, rs);
        try {
            if (con != null) con.close();
        } catch (SQLException e) {
        }
    }
}


