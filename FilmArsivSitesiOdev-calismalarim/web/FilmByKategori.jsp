<%@page import="filmarsivi.model.Kullanici"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="filmarsivi.dao.FilmDao"%>
<%@page import="filmarsivi.model.Film"%>
<%@page import="filmarsivi.dao.KategoriDao"%>
<%@page import="filmarsivi.model.Kategori"%>
<%@page import="java.util.List"%>


<%
    Kullanici kullanici = (Kullanici) session.getAttribute("kullanici");
    Integer kullaniciId = (Integer) session.getAttribute("kullanici_id");
    Boolean admin = (Boolean) session.getAttribute("admin");
    if (admin == null) admin = false;
    
    String kategoriIdStr = request.getParameter("kategoriId");
    int kategoriId = Integer.parseInt(kategoriIdStr);  

    KategoriDao kategoriDao = new KategoriDao();
    Kategori kategori = kategoriDao.findById(kategoriId);

   
    FilmDao filmDao = new FilmDao();
    List<Film> filmList = filmDao.listByKategoriId(kategoriId); 
    

%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= kategori.getKategori_ad() %> Filmleri</title>
    <style>
        a {
    text-decoration: none;
    color: inherit;
}

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

        .movies-container {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
            gap: 20px;
        }

        .movie {
            background-color: #fff;
            padding: 10px;
            border-radius: 5px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            text-align: center;
        transition: transform 0.3s ease-in-out;
}
.movie:hover {
  transform: scale(1.05);
}

        .movie-image {
            width: 100%;
            height: 300px;
            object-fit: cover;
            border-radius: 8px;
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

       
        .movie-link {
            display: inline-block;
            background-color: #45a049;
            padding: 10px;
            border-radius: 5px;
            text-decoration: none;
            color: white;
            margin-top: 10px;
        }

        .movie-link:hover {
            background-color: #387c3f;
        }
 
    </style>
    
</head>
<body>
 <jsp:include page="Ortak/Header.jsp" />
    <hr/>
<div class="container">
   
    <h2><%= kategori.getKategori_ad() %> Kategorisindeki Filmler</h2>

    <div class="movies-container">
    <% for (Film film : filmList) { %>
        <a href="FilmDetaysayfa.jsp?id=<%= film.getId() %>" style="text-decoration: none; color: inherit;">
            <div class="movie">
                <img src="<%= film.getFilmResimUrl() %>" alt="<%= film.getFilmBaslik() %>" class="movie-image">
                <div class="movie-info">
                    <h4><%= film.getFilmBaslik() %></h4>
                    <p class="movie-rating">Puan: <%= film.getFilmPuan() %></p>
                </div>
            </div>
        </a>
    <% } %>
</div>

</div>

</body>
</html>
