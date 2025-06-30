<%-- 
    Document   : Favoriler
    Created on : 26 Nis 2025, 11:49:26
    Author     : alperensarac
--%>


<%@page import="filmarsivi.model.Kullanici"%>
<%@page import="filmarsivi.model.Film"%>
<%@page import="filmarsivi.dao.FilmDao"%>
<%@page import="filmarsivi.model.Favori"%>
<%@page import="filmarsivi.dao.FavoriDao"%>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>


<%
    Kullanici kullanici = (Kullanici) session.getAttribute("kullanici");
    Integer kullaniciId = (Integer) session.getAttribute("kullanici_id");
    Boolean admin = (Boolean) session.getAttribute("admin");
    if (admin == null) admin = false;

    String favoriSilId = request.getParameter("favoriSilId");
    if (favoriSilId != null && kullaniciId != null) {
        int filmId = Integer.parseInt(favoriSilId);

        FavoriDao favoriDao = new FavoriDao();
        favoriDao.delete(kullaniciId, filmId);

       
        response.sendRedirect("Favoriler.jsp");
        return;
    }

    
    FavoriDao favoriDao = new FavoriDao();
    List<Favori> favoriList = favoriDao.listByKullaniciId(kullaniciId);

    FilmDao filmDao = new FilmDao();
    List<Film> tumFilmler = filmDao.listAll();
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
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); 
  gap: 20px;
  justify-content: center;
  padding: 20px;
}

.movies {
  display: flex;
  gap: 20px;
  transition: transform 0.3s ease-in-out; 
}




.movie:hover {
  transform: scale(1.05);
}



.movie {
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  padding: 10px;
  transition: transform 0.2s;
}

.movie-image {
  width: 200px;
  height: 300px;
  object-fit: cover;
  border-radius: 5px;
  margin-bottom: 10px;
}

.movie-info h4 {
  font-size: 16px;
  margin: 10px 0;
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
    <script>

function scrollRight() {
  const movies = document.querySelector('.movies');
  const totalMovies = document.querySelectorAll('.movie').length;
  
  if (currentIndex < totalMovies - 3) { 
    currentIndex++;
    movies.style.transform = `translateX(-${currentIndex * 220}px)`;
  }
}
    </script>
</head>
<body>
 <jsp:include page="Ortak/Header.jsp" />
        <hr/>

  <div class="container">
    <div class="category">
      
      <!-- Film Kategorisi 1 -->
      <div class="movie-category">
        <h3>Favoriler</h3>
        <div class="movies-container">
  <div class="movies">
  <%
    for (Favori favori : favoriList) {
        Film film = null;
        for (Film f : tumFilmler) {
            if (f.getId() == favori.getFilmId()) {
                film = f;
                break;
            }
        }
        if (film != null) {
  %>
    <div class="movie">
      <a href="FilmDetaysayfa.jsp?id=<%= film.getId() %>" style="text-decoration: none; color: inherit;">
        <img src="<%= film.getFilmResimUrl() %>" class="movie-image" />
        <div class="movie-info" style="position: relative;">
          <h4 style="display: flex; align-items: center; justify-content: space-between;">
            <span><%= film.getFilmBaslik() %></span>
            <a href="Favoriler.jsp?favoriSilId=<%= film.getId() %>" onclick="event.stopPropagation();">
              <img 
                src="Resimler/favori_simgesi_filled.png" 
                alt="Favori" 
                style="width: 20px; height: 20px; cursor: pointer;" />
            </a>
          </h4>
          <p class="movie-rating">Puan: <%= film.getFilmPuan() %></p>
        </div>
      </a>
    </div>
  <%
        }
    }
  %>
</div>


          
          <!-- Sa? Ok -->
          <button class="scroll-button" onclick="scrollRight()">T?kla</button>
        </div>
      </div>
    </div>
  </div>
</body>
</html>
