<%@page import="filmarsivi.model.Kategori"%>
<%@page import="filmarsivi.model.Film"%>
<%@page import="filmarsivi.dao.FilmDao"%>
<%@page import="filmarsivi.dao.KategoriDao"%>
<%@page import="java.util.List"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    request.setCharacterEncoding("UTF-8");
    Boolean adminMi = (Boolean) session.getAttribute("admin");
    if (adminMi == null || !adminMi) {
        response.sendRedirect("../FilmlerAnasayfa.jsp");
        return;
    }

    String idStr = request.getParameter("filmId");
    if (idStr == null || idStr.trim().isEmpty()) {
        out.println("<h2 style='color:red;'>Film ID eksik!</h2>");
        return;
    }

    int id = Integer.parseInt(idStr);
    FilmDao filmDao = new FilmDao();
    Film film = filmDao.findById(id);
    if (film == null) {
        out.println("<h2 style='color:red;'>Film bulunamadı!</h2>");
        return;
    }

    KategoriDao kategoriDao = new KategoriDao();
    List<Kategori> kategoriler = kategoriDao.listAll();

    // Güncelleme işlemi
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String baslik = request.getParameter("baslik");
        String cikisYili = request.getParameter("cikisYili");
        String sure = request.getParameter("sure");
        String aciklama = request.getParameter("aciklama");
        String puanStr = request.getParameter("puan");
        String kategoriStr = request.getParameter("filmKategoriId");
        String resimUrl = request.getParameter("filmResimUrl");
        String videoUrl = request.getParameter("filmVideoUrl");

        double puan = Double.parseDouble(puanStr.replace(",", "."));
        int kategoriId = Integer.parseInt(kategoriStr);

        Film guncelFilm = new Film();
        guncelFilm.setId(id);
        guncelFilm.setFilmBaslik(baslik);
        guncelFilm.setFilmCikisYili(cikisYili);
        guncelFilm.setFilmSure(sure);
        guncelFilm.setFilmAciklama(aciklama);
        guncelFilm.setFilmKategoriId(kategoriId);
        guncelFilm.setFilmPuan(puan);
        guncelFilm.setFilmResimUrl(resimUrl);
        guncelFilm.setFilmUrl(videoUrl);

        boolean basarili = filmDao.update(guncelFilm);
        if (basarili) {
            response.sendRedirect("../FilmlerAnasayfa.jsp");
            return;
        } else {
            out.println("<h3 style='color:red;'>❌ Güncelleme başarısız.</h3>");
        }
    }
%>

<!DOCTYPE html>
<html lang="tr">
<head>
  <meta charset="UTF-8">
  <title>Film Güncelle</title>
  <style>
    body {
      background-color: #f0e6e3;
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
    }
    .film_form {
      background-color: #ffffff;
      border-radius: 15px;
      padding: 30px;
      display: flex;
      gap: 20px;
      box-shadow: 0 0 100px rgba(29, 28, 28, 0.15);
    }
    .sol_panel, .sag_panel {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    input, select, textarea, button {
      border-radius: 15px;
      border: 1px solid rgb(61, 56, 56);
      padding: 10px;
      font-size: 12px;
    }
    textarea {
      resize: none;
      height: 100px;
    }
    .image-preview {
      width: 150px;
      height: 150px;
      background-color: white;
      border-radius: 12px;
      border: 1px solid #ccc;
    }
    button {
      background-color: #663434;
      color: white;
      font-weight: bold;
      cursor: pointer;
    }
  </style>

  <script>
    function updateImagePreview() {
      const url = document.getElementById("resimUrl").value;
      document.getElementById("resimPreview").src = url;
    }
  </script>
</head>
<body>
    <div style="position: fixed; top: 0; left: 0; width: 100%; z-index: 999; background-color: white;">
    <jsp:include page="../Ortak/Header.jsp" />
</div>
  <form class="film_form" method="POST">
    <!-- Sol Panel -->
    <div class="sol_panel">
      <label for="filmResimUrl">Film Afişi (Resim URL)</label>
      <input type="text" name="filmResimUrl" id="resimUrl" value="<%= film.getFilmResimUrl() %>" oninput="updateImagePreview()" required>
      <img id="resimPreview" class="image-preview" src="<%= film.getFilmResimUrl() %>" alt="Afiş Önizleme">

      <label for="filmVideoUrl">Film Videosu (YouTube Linki)</label>
      <input type="text" name="filmVideoUrl" value="<%= film.getFilmUrl() != null ? film.getFilmUrl() : "" %>" required>
    </div>

    <!-- Sağ Panel -->
    <div class="sag_panel">
      <input type="text" name="baslik" value="<%= film.getFilmBaslik() %>" required>
      <textarea name="aciklama" required><%= film.getFilmAciklama() %></textarea>

      <select name="filmKategoriId" required>
        <option value="" disabled>Kategori Seçin</option>
        <% for (Kategori kategori : kategoriler) { 
             boolean seciliMi = (film.getFilmKategoriId() == kategori.getId()); %>
            <option value="<%= kategori.getId() %>" <%= seciliMi ? "selected" : "" %>>
                <%= kategori.getKategori_ad() %>
            </option>
        <% } %>
      </select>

      <input type="text" name="puan" value="<%= film.getFilmPuan() %>" required>
      <input type="text" name="cikisYili" value="<%= film.getFilmCikisYili() %>" required>
      <input type="text" name="sure" value="<%= film.getFilmSure() %>" required>
      <button type="submit">Güncelle</button>
    </div>
  </form>
</body>
</html>
