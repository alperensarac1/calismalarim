/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */package filmarsivi.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtilities {

    private static final String URL = "jdbc:mysql://localhost:3306/film_arsiv_sitesi?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";  // Kullanıcı adı
private static final String PASSWORD = "";  // Şifre (Eğer şifre varsa, burada belirtmelisiniz)

    // Sürücü sadece bir kez yüklenir (sınıf yüklenirken)
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver"); // 5.1.13 için
        } catch (ClassNotFoundException e) {
        }
    }

    // Veritabanı bağlantısını döner
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

