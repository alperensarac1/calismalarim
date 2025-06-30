<%-- 
    Document   : FilmlerTop100
    Created on : 27 Nis 2025, 15:29:49
    Author     : alperensarac
--%>

<%@page import="filmarsivi.model.Kullanici" %>
<%@ page import="filmarsivi.dao.KategoriDao" %>
<%@ page import="filmarsivi.model.Kategori" %>
<%@ page import="filmarsivi.dao.FilmDao" %>
<%@ page import="filmarsivi.model.Film" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    Kullanici kullanici = (Kullanici) session.getAttribute("kullanici");
    Integer kullaniciId = (Integer) session.getAttribute("kullanici_id");
    Boolean admin = (Boolean) session.getAttribute("admin");
    if (admin == null) admin = false;
%>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Film Sitesi - Anasayfa</title>
  <style>
      body {
  font-family: Arial, sans-serif;
  background-color: #FEE0DF;
  margin: 0;
  padding: 0;
}

.container {
  width: 90%;
  margin: 0 auto;
}

.category {
  margin-top: 40px;
}

h2 {
  text-align: center;
  font-size: 24px;
  margin-bottom: 20px;
}

.movie-category {
  margin-bottom: 40px;
}

.movie-category h3 {
  font-size: 20px;
  margin-bottom: 15px;
}

.movies-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.movies {
  display: flex;
  gap: 20px;
  transition: transform 0.3s ease-in-out;
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr); 
  gap: 20px;
  margin-top: 20px;
}

.movie {
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  overflow: hidden;
transition: transform 0.3s ease-in-out;
}
.movie:hover {
  transform: scale(1.05);
}

.movie-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.movie-info {
  padding: 10px;
}

.movie-rating {
  font-size: 14px;
  color: #555;
}


.scroll-button {
  position: absolute;
  right: 0;
  background-color: transparent;
  border: none;
  font-size: 30px;
  color: white;
  cursor: pointer;
  z-index: 1;
}
       

  </style>

</head>
<body>

 <jsp:include page="Ortak/Header.jsp" />
    <hr/>


  <div class="container">
  <h2>Top 100</h2>
  <div class="movie-grid">
    <%
      FilmDao filmDao = new FilmDao();
      List<Film> tumFilmler = filmDao.listTop100ByRating();
      for (Film film : tumFilmler) {
    %>
      <a href="FilmDetaysayfa.jsp?id=<%= film.getId() %>" style="text-decoration: none; color: inherit;">
        <div class="movie">
          <img src="<%= film.getFilmResimUrl() %>" alt="<%= film.getFilmBaslik() %>" class="movie-image">
          <div class="movie-info">
            <h4><%= film.getFilmBaslik() %></h4>
            <p class="movie-rating">Puan: <%= film.getFilmPuan() %></p>
          </div>
        </div>
      </a>
    <%
      }
    %>
  </div>
</div>


  <!-- Footer -->
  <div class="footer">
    <p>&copy; 2025 Film Sitesi. Tüm Hakları Saklıdır.</p>
  </div>

</body>
</html>
