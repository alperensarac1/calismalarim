<%@page import="filmarsivi.model.Kategori"%>
<%@page import="filmarsivi.dao.KategoriDao"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Boolean admin = (Boolean) session.getAttribute("admin");
    if (admin == null || !admin) {
        response.sendRedirect("KullaniciGiris.jsp");
        return;
    }

    KategoriDao kategoriDao = new KategoriDao();
    List<Kategori> kategoriler = kategoriDao.listAll();
%>

<!DOCTYPE html>
<html lang="tr">
<head>
  <meta charset="UTF-8">
  <title>Yeni Film Ekle</title>
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
      gap: 30px;
      box-shadow: 0 0 100px rgba(29, 28, 28, 0.15);
    }

    .sol_panel, .sag_panel {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }

    input, select, textarea, button {
      border-radius: 12px;
      border: 1px solid rgb(61, 56, 56);
      padding: 10px;
      font-size: 13px;
    }

    textarea {
      resize: none;
      height: 100px;
    }

    .image-preview {
      width: 150px;
      height: 200px;
      background-color: white;
      border-radius: 12px;
      border: 1px solid #ccc;
      object-fit: cover;
    }

    button {
      background-color: #663434;
      color: white;
      font-weight: bold;
      cursor: pointer;
    }

    label {
      font-weight: bold;
      font-size: 13px;
    }
  </style>

  <script>
    function updateImagePreview() {
      const url = document.getElementById("resimUrl").value;
      const preview = document.getElementById("resimPreview");
      preview.src = url;
    }
  </script>
</head>
<body>
<div style="position: fixed; top: 0; left: 0; width: 100%; z-index: 999; background-color: white;">
    <jsp:include page="../Ortak/Header.jsp" />
</div>
 <form class="film_form" action="../Islemler/FilmEkleIslem.jsp" method="POST">
  <!-- Sol Panel: Resim ve Video URL -->
  <div class="sol_panel">
    <label for="filmResimUrl">Film Afişi (Resim URL)</label>
    <input type="text" name="filmResimUrl" id="resimUrl" oninput="updateImagePreview()" placeholder="https://example.com/image.jpg" required>
    <img id="resimPreview" class="image-preview" src="" alt="Afiş Önizleme">

    <label for="filmVideoUrl">Film Videosu (YouTube Linki)</label>
    <input type="text" name="filmVideoUrl" placeholder="https://www.youtube.com/embed/..." required>
  </div>

  <!-- Sağ Panel: Metin Bilgileri -->
  <div class="sag_panel">
    <input type="text" name="filmBaslik" placeholder="Film Adı" required>
    <textarea name="filmAciklama" placeholder="Film Açıklaması.." required></textarea>

    <select name="filmKategoriId" required>
      <option value="" disabled selected>Kategori Seçin</option>
      <% for (Kategori kategori : kategoriler) { %>
        <option value="<%= kategori.getId() %>"><%= kategori.getKategori_ad() %></option>
      <% } %>
    </select>

    <input type="text" name="filmPuan" placeholder="IMDB Puanı" required>
    <input type="text" name="filmCikisYili" placeholder="Yayınlanma Yılı" required>
    <input type="text" name="filmSure" placeholder="Süre (dakika)" required>

    <button type="submit">Film Ekle</button>
  </div>
</form>

<script>
  function updateImagePreview() {
    const url = document.getElementById('resimUrl').value;
    document.getElementById('resimPreview').src = url;
  }
</script>


</body>
</html>
