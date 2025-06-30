<%-- 
    Document   : KategoriEkle
    Created on : 13 May 2025, 11:41:49
    Author     : alperensarac
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Boolean admin = (Boolean) session.getAttribute("admin");
    if (admin == null || !admin) {
        response.sendRedirect("KullaniciGiris.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="tr">
<head>
  <meta charset="UTF-8">
  <title>Yeni Kategori Ekle</title>
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

    input, textarea, button {
      border-radius: 12px;
      border: 1px solid rgb(61, 56, 56);
      padding: 10px;
      font-size: 13px;
    }

    .image-preview {
      width: 150px;
      height: 150px;
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
    function updateKategoriImagePreview() {
      const url = document.getElementById("kategoriResimUrl").value;
      const preview = document.getElementById("kategoriResimPreview");
      preview.src = url;
    }
  </script>
</head>
<body>
  <div style="position: fixed; top: 0; left: 0; width: 100%; z-index: 999; background-color: white;">
    <jsp:include page="../Ortak/Header.jsp" />
  </div>

  <form class="film_form" action="../Islemler/KategoriEkleIslem.jsp" method="POST">
    <!-- Sol Panel: Resim -->
    <div class="sol_panel">
      <label for="kategoriResim">Kategori Resmi (URL)</label>
      <input type="text" name="kategoriResim" id="kategoriResimUrl" oninput="updateKategoriImagePreview()" placeholder="https://example.com/image.jpg" required>
      <img id="kategoriResimPreview" class="image-preview" src="" alt="Resim Önizleme">
    </div>

    <!-- Sağ Panel: Ad -->
    <div class="sag_panel">
      <input type="text" name="kategoriAd" placeholder="Kategori Adı" required>
      <button type="submit">Kategori Ekle</button>
    </div>
  </form>
</body>
</html>
