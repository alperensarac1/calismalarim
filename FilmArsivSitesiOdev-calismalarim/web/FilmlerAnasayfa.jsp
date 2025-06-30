<%@page import="filmarsivi.model.Kullanici"%>
<%@page import="filmarsivi.model.Film"%>
<%@page import="filmarsivi.dao.FilmDao"%>
<%@ page import="filmarsivi.dao.KategoriDao" %>
<%@ page import="filmarsivi.model.Kategori" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>


<%
    Kullanici kullanici = (Kullanici) session.getAttribute("kullanici");
    Integer kullaniciId = (Integer) session.getAttribute("kullanici_id");
    Boolean admin = (Boolean) session.getAttribute("admin");
    if (admin == null) admin = false;
    KategoriDao kategoriDao = new KategoriDao();
    List<Kategori> kategoriListesi = kategoriDao.listAll();
    FilmDao filmDao = new FilmDao();
    List<Film> vizyonaGirecekFilmler = filmDao.listByVizyonaGirecek();
    List<Film> vizyondakiFilmler = filmDao.listByVizyondaki();
%>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Film Sitesi - Anasayfa</title>
  <style>
      body {
          width: 1920px;
          height: 1080px;
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
    display:flex;
    flex-direction: row;
}

h2 {
    text-align: left;
    font-size: 24px;
    margin-left: 50px;
    margin-bottom: 20px;
}

.movie-category {
    margin-bottom: 20px;
}


.category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin: 10px;
    text-align: center;
    transition: transform 0.3s ease-in-out;
}
.category-item:hover{
    transform: scale(1.05);
}

.category-item .category-image {
    width: 150px;
    height: 250px;
    object-fit: cover;
    border-radius: 8px;
}

.category-name {
    margin-top: 10px;
    font-size: 16px;
    font-weight: bold;
    color: #333;
}

.movies-container {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    overflow: hidden;
    position: relative;
}

.movie-carousel-wrapper {
    position: relative;
    width: 100%;
    padding-left: 50px;
    margin: 20px 0;
}
a {
    text-decoration: none;
    color: inherit;
}

.movies {
    display: flex;
    gap: 20px;
    scroll-behavior: smooth;
    overflow: hidden;
    width: 100%;
}
.movie:hover {
  transform: scale(1.05);
}

.movie {
    background-color: #fff;
    padding: 10px;
    border-radius: 5px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    text-align: center;
    width: 200px;
    flex: 0 0 auto;
    transition: transform 0.3s ease-in-out;
}


.movie-image {
    width: 180px;
    height: 300px;
    object-fit: cover;
    border-radius: 10px;
}

.movie-info h4 {
    font-size: 16px;
    margin: 10px 0;
}

.movie-rating {
    font-size: 14px;
    color: #555;
}


.scroll-btn {
    background: rgba(255, 255, 255, 0.7);
    border: none;
    font-size: 50px;
    cursor: pointer;
    position: absolute;
    top: 45%;
    transform: translateY(-50%);
    z-index: 5;
    padding: 30px;
    border-radius: 50%;
    box-shadow: 0 0 5px rgba(0,0,0,0.3);
    transition: background 0.2s;
}

.scroll-btn:hover {
    background: #eee;
}

.scroll-btn.left {
    left: 10px;
    margin-right: 50px;
}

.scroll-btn.right {
    right: 1px;
    margin-left: 50px;
}


      

  
  </style>
<script>
function scrollMovies(id, direction) {
    const container = document.getElementById(id);
    const scrollAmount = 220;
    const maxScroll = container.scrollWidth - container.clientWidth;

    if (direction === 1) {
        if (container.scrollLeft + scrollAmount >= maxScroll) {
            container.scrollLeft = 0; 
        } else {
            container.scrollLeft += scrollAmount;
        }
    } else {
        if (container.scrollLeft - scrollAmount <= 0) {
            container.scrollLeft = maxScroll; 
        } else {
            container.scrollLeft -= scrollAmount;
        }
    }
}
</script>

  
</head>
<body>

<jsp:include page="Ortak/Header.jsp" />

<hr/>
  <div class="container">
    
    <h2>Kategoriler</h2>
    <div class="category">
     
        <% for (Kategori kategori : kategoriListesi) { %>
        <div class="movie-category">
           
            <div class="category-item">
                
                <a href="FilmByKategori.jsp?kategoriId=<%= kategori.getId() %>" class="category-link">
                    <img src="<%= kategori.getKategori_resim() %>" alt="<%= kategori.getKategori_ad() %>" class="category-image">
                    <h3 class="category-name"><%= kategori.getKategori_ad() %></h3>
                </a>
            </div>
        </div>
        <% } %>
    </div>
</div>


<h2>Vizyona Girecek Filmler</h2>
<div class="movie-carousel-wrapper">
    <button class="scroll-btn left" onclick="scrollMovies('vizyonaGirecek', -1)">‹</button>

    <div class="movies-container">
        <div class="movies" id="vizyonaGirecek">
            <% for (Film film : vizyonaGirecekFilmler) { %>
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
    </div>

    <button class="scroll-btn right" onclick="scrollMovies('vizyonaGirecek', 1)">›</button>
</div>

<h2>Vizyondaki Filmler</h2>
<div class="movie-carousel-wrapper">
    
    <button class="scroll-btn left" onclick="scrollMovies('vizyondaki', -1)">‹</button>

    <div class="movies-container">
        <div class="movies" id="vizyondaki">
            <% for (Film film : vizyondakiFilmler) { %>
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
    </div>

    
    <button class="scroll-btn right" onclick="scrollMovies('vizyondaki', 1)">›</button>
</div>




 
  <div class="footer">
    <p>&copy; 2025 Film Sitesi. Tüm Hakları Saklıdır.</p>
  </div>

</body>
</html>
