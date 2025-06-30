<%@page import="filmarsivi.dao.FilmDao"%>
<%@page import="filmarsivi.model.Film"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    String filmBaslik = request.getParameter("filmBaslik");
    FilmDao filmDao = new FilmDao();
    List<Film> bulunanFilmler = filmDao.searchByTitle(filmBaslik);

    if (bulunanFilmler.size() == 1) {
        response.sendRedirect("FilmDetaysayfa.jsp?id=" + bulunanFilmler.get(0).getId());
        return;
    }
%>


<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Arama Sonuçları</title>
    <style>
                body {
  font-family: Arial, sans-serif;
  background-color: #FEE0DF;
  margin: 0;
  padding: 0;
}
a {
    text-decoration: none;
    color: inherit;
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

.movie {
  background-color: #fff;
  padding: 10px;
  border-radius: 5px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  min-width: 200px;
}

.movie-image {
  width: 100%;
  height: auto;
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

        .grid-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 250px)); /* sabit genişlik */
    justify-content: center; /* ortala */
    gap: 20px;
    padding: 20px;
}


        .film-kutu {
            border: 1px solid #ccc;
            border-radius: 8px;
            padding: 10px;
            text-align: center;
            transition: 0.3s;
            background-color: #f9f9f9;
            text-decoration: none;
            color: inherit;
        }

        .film-kutu:hover {
            background-color: #ececec;
            transform: scale(1.02);
        }

        .film-kutu img {
            width: 100%;
            height: 300px;
            object-fit: cover;
            border-radius: 6px;
        }

        .film-title {
            font-weight: bold;
            margin-top: 10px;
            font-size: 18px;
        }

        .film-info {
            font-size: 14px;
            color: gray;
        }
        

  
  
    </style>
</head>
<body>
    <jsp:include page="Ortak/Header.jsp" />
    <hr/>
    <h2>Arama Sonuçları</h2>
    <% if (bulunanFilmler.isEmpty()) { %>
        <p>Aradığınız film bulunamadı.</p>
    <% } else { %>
        <div class="grid-container">
    <% for (Film film : bulunanFilmler) { %>
                <div class="movie">
                   
                    <a href="FilmDetaysayfa.jsp?id=<%= film.getId() %>">
                        <img src="<%= film.getFilmResimUrl() %>" alt="<%= film.getFilmBaslik() %>" class="movie-image">
                        <div class="movie-info">
                            <h4><%= film.getFilmBaslik() %></h4>
                            <p class="movie-rating">Puan: <%= film.getFilmPuan() %></p>
                        </div>
                    </a>
                </div>
                <% } %>
</div>
    <% } %>
    

<br/>
<div style="text-align:center;">
    <a href="FilmlerAnasayfa.jsp">Anasayfaya Dön</a>
</div>

</body>
</html>
