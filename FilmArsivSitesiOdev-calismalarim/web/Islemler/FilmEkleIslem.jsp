<%@ page import="filmarsivi.dao.FilmDao, filmarsivi.model.Film" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    request.setCharacterEncoding("UTF-8");

    Boolean adminMi = (Boolean) session.getAttribute("admin");
    if (adminMi == null || !adminMi) {
        response.sendRedirect("../FilmlerAnasayfa.jsp");
        return;
    }

    String filmBaslik = request.getParameter("filmBaslik");
    String filmCikisYili = request.getParameter("filmCikisYili");
    String filmSure = request.getParameter("filmSure");
    String filmAciklama = request.getParameter("filmAciklama");
    String filmPuanStr = request.getParameter("filmPuan");
    String kategoriStr = request.getParameter("filmKategoriId");
    String filmVideoUrl = request.getParameter("filmVideoUrl");
    String filmResimUrl = request.getParameter("filmResimUrl"); 

    try {
        double filmPuan = Double.parseDouble(filmPuanStr.replace(",", "."));
        int kategoriId = Integer.parseInt(kategoriStr);

        // Film nesnesi oluşturuluyor
        Film film = new Film();
        film.setFilmBaslik(filmBaslik);
        film.setFilmCikisYili(filmCikisYili);
        film.setFilmSure(filmSure);
        film.setFilmAciklama(filmAciklama);
        film.setFilmPuan(filmPuan);
        film.setFilmKategoriId(kategoriId);
        film.setFilmResimUrl(filmResimUrl);
        film.setFilmUrl(filmVideoUrl);     

        FilmDao filmDao = new FilmDao();
        boolean basarili = filmDao.insert(film);

        if (basarili) {
            response.sendRedirect("../FilmlerAnasayfa.jsp");
        } else {
            out.println("<h2 style='color:red;'>❌ Film eklenirken hata oldu.</h2>");
        }

    } catch (Exception e) {
        out.println("<h2 style='color:red;'>⚠️ Hata: " + e.getMessage() + "</h2>");
    }
%>
