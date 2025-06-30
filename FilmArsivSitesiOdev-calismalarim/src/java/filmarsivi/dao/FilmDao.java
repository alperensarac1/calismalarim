package filmarsivi.dao;

import filmarsivi.model.Film;
import filmarsivi.utils.BaseDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FilmDao extends BaseDao {

    // Film tablosundaki veriyi Film nesnesine çevirir
    private Film mapFilm(ResultSet rs) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setFilmBaslik(rs.getString("film_baslik"));
        film.setFilmCikisYili(rs.getString("film_cikis_yili"));
        film.setFilmSure(rs.getString("film_sure"));
        film.setFilmAciklama(rs.getString("film_aciklama"));
        film.setFilmKategoriId(rs.getInt("film_kategori_id"));
        film.setFilmResimUrl(rs.getString("film_resim_url"));
        film.setFilmPuan(rs.getDouble("film_puan"));
        film.setFilmUrl(rs.getString("film_url"));
        return film;
    }

    public List<Film> listTop100ByRating() {
        List<Film> filmList = new ArrayList<>();
        String sql = "SELECT * FROM Filmler ORDER BY film_puan DESC LIMIT 100";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                filmList.add(mapFilm(rs));
            }

        } catch (Exception e) {
        }

        return filmList;
    }

    public List<Film> listAll() {
        List<Film> filmList = new ArrayList<>();
        String sql = "SELECT * FROM Filmler ORDER BY id DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                filmList.add(mapFilm(rs));
            }

        } catch (Exception e) {
        }

        return filmList;
    }

    public List<Film> listByKategoriId(int kategoriId) {
        List<Film> filmList = new ArrayList<>();
        String sql = "SELECT * FROM Filmler WHERE film_kategori_id = ? ORDER BY id DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, kategoriId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filmList.add(mapFilm(rs));
                }
            }

        } catch (Exception e) {
        }

        return filmList;
    }

    public List<Film> listSon5Film() {
        List<Film> filmList = new ArrayList<>();
        String sql = "SELECT * FROM Filmler ORDER BY id DESC LIMIT 5";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                filmList.add(mapFilm(rs));
            }

        } catch (Exception e) {
        }

        return filmList;
    }

    public List<Film> listSon5FilmByKategori(int kategoriId) {
        List<Film> filmList = new ArrayList<>();
        String sql = "SELECT * FROM Filmler WHERE film_kategori_id = ? ORDER BY id DESC LIMIT 5";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, kategoriId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filmList.add(mapFilm(rs));
                }
            }

        } catch (Exception e) {
        }

        return filmList;
    }

    public List<Film> searchByTitle(String title) {
        List<Film> filmList = new ArrayList<>();
        String sql = "SELECT * FROM Filmler WHERE film_baslik LIKE ? ORDER BY id DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + title + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filmList.add(mapFilm(rs));
                }
            }

        } catch (Exception e) {
        }

        return filmList;
    }

    public boolean insert(Film film) {
        String sql = "INSERT INTO Filmler (film_baslik, film_cikis_yili, film_sure, film_aciklama, film_kategori_id, film_resim_url, film_puan, film_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, film.getFilmBaslik());
            ps.setString(2, film.getFilmCikisYili());
            ps.setString(3, film.getFilmSure());
            ps.setString(4, film.getFilmAciklama());
            ps.setInt(5, film.getFilmKategoriId());
            ps.setString(6, film.getFilmResimUrl());
            ps.setDouble(7, film.getFilmPuan());
            ps.setString(8, film.getFilmUrl());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
        }

        return false;
    }

    public boolean update(Film film) {
        String sql = "UPDATE Filmler SET film_baslik=?, film_cikis_yili=?, film_sure=?, film_aciklama=?, film_kategori_id=?, film_resim_url=?, film_puan=?, film_url=? WHERE id=?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, film.getFilmBaslik());
            ps.setString(2, film.getFilmCikisYili());
            ps.setString(3, film.getFilmSure());
            ps.setString(4, film.getFilmAciklama());
            ps.setInt(5, film.getFilmKategoriId());
            ps.setString(6, film.getFilmResimUrl());
            ps.setDouble(7, film.getFilmPuan());
            ps.setString(8, film.getFilmUrl());
            ps.setInt(9, film.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
        }

        return false;
    }

    public boolean delete(int filmId) {
        String sql = "DELETE FROM Filmler WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, filmId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
        }

        return false;
    }

    public List<Film> listByFavori() {
        List<Film> filmList = new ArrayList<>();
        String sql = "SELECT f.* FROM Favoriler fav JOIN Filmler f ON fav.film_id = f.id GROUP BY f.id ORDER BY COUNT(*) DESC LIMIT 100";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                filmList.add(mapFilm(rs));
            }

        } catch (Exception e) {
        }

        return filmList;
    }

    public List<Film> listByVizyonaGirecek() {
        List<Film> filmList = new ArrayList<>();
        String sql = "SELECT * FROM Filmler WHERE film_cikis_yili LIKE '2025'";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                filmList.add(mapFilm(rs));
            }

        } catch (Exception e) {
        }

        return filmList;
    }

    public List<Film> listByVizyondaki() {
        List<Film> filmList = new ArrayList<>();
        String sql = "SELECT * FROM Filmler WHERE film_cikis_yili LIKE '2026'";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                filmList.add(mapFilm(rs));
            }

        } catch (Exception e) {
        }

        return filmList;
    }

    public Film findById(int filmId) {
        Film film = null;
        String sql = "SELECT * FROM Filmler WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, filmId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                film = mapFilm(rs);
            }

        } catch (Exception e) {
        }

        return film;
    }
public List<String> getAllResimYollari() {
    List<String> yolListesi = new ArrayList<>();
    String sql = "SELECT film_resim_url FROM Filmler";

    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            yolListesi.add(rs.getString("film_resim_url"));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return yolListesi;
} 
}
