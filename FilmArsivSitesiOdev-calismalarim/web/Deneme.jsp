<%@ page import="java.io.*, javax.servlet.http.Part" %>
<%@ page import="filmarsivi.dao.FilmDao, filmarsivi.model.Film" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    request.setCharacterEncoding("UTF-8");

    // Admin kontrolü
    Boolean adminMi = (Boolean) session.getAttribute("admin");
    if (adminMi == null || !adminMi) {
        response.sendRedirect("KullaniciGiris.jsp");
        return;
    }

    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String filmBaslik = request.getParameter("filmBaslik");
        String filmCikisYili = request.getParameter("filmCikisYili");
        String filmSure = request.getParameter("filmSure");
        String filmAciklama = request.getParameter("filmAciklama");
        String filmPuanStr = request.getParameter("filmPuan");
        String kategoriStr = request.getParameter("filmKategoriId");

        try {
            double filmPuan = Double.parseDouble(filmPuanStr.replace(",", "."));
            int kategoriId = Integer.parseInt(kategoriStr);

            Part filePart = request.getPart("filmResim");
            String orijinalDosyaAdi = filePart.getSubmittedFileName();
            String uzanti = orijinalDosyaAdi.substring(orijinalDosyaAdi.lastIndexOf("."));
            String ilkKelime = filmBaslik.trim().split("\\s+")[0].toLowerCase();
            String yeniDosyaAdi = ilkKelime + uzanti;

            // ✅ Sabit yol
            String sabitYol = "/Users/alperensarac/NetBeansProjects/FilmArsivSitesiOdev/web/Resimler";

            File klasor = new File(sabitYol);
            if (!klasor.exists()) klasor.mkdirs();

            InputStream input = filePart.getInputStream();
            FileOutputStream fos = new FileOutputStream(new File(klasor, yeniDosyaAdi));
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            input.close();

            String webYolu = "Resimler/" + yeniDosyaAdi;

            Film film = new Film();
            film.setFilmBaslik(filmBaslik);
            film.setFilmCikisYili(filmCikisYili);
            film.setFilmSure(filmSure);
            film.setFilmAciklama(filmAciklama);
            film.setFilmPuan(filmPuan);
            film.setFilmKategoriId(kategoriId);
            film.setFilmResimUrl(webYolu);

            FilmDao filmDao = new FilmDao();
            boolean basarili = filmDao.insert(film);

            if (basarili) {
                response.sendRedirect("FilmlerAnasayfa.jsp");
            } else {
                out.println("<p style='color:red;'>❌ Film eklenemedi.</p>");
            }

        } catch (Exception e) {
            out.println("<p style='color:red;'>⚠️ Hata: " + e.getMessage() + "</p>");
        }
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Film Ekle</title>
</head>
<body>
    <h2>Yeni Film Ekle</h2>
    <form method="POST" enctype="multipart/form-data">
        Başlık: <input type="text" name="filmBaslik" required><br><br>
        Çıkış Yılı: <input type="text" name="filmCikisYili" required><br><br>
        Süre (dakika): <input type="text" name="filmSure" required><br><br>
        Açıklama: <textarea name="filmAciklama" required></textarea><br><br>
        Puan (örn. 7.5): <input type="text" name="filmPuan" required><br><br>
        Kategori ID: <input type="text" name="filmKategoriId" required><br><br>
        Film Resmi: <input type="file" name="filmResim" accept="image/*" required><br><br>
        <input type="submit" value="Filmi Ekle">
    </form>
</body>
</html>
