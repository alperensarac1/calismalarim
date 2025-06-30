<%-- 
    Document   : FilmDetaysayfa
    Created on : 26 Nis 2025, 11:40:07
    Author     : alperensarac
--%>

<%@page import="filmarsivi.model.YorumBegeni"%>
<%@page import="filmarsivi.dao.YorumBegeniDao"%>
<%@page import="java.time.LocalDateTime"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="filmarsivi.model.Kullanici"%>
<%@page import="filmarsivi.dao.FilmDao"%>
<%@page import="filmarsivi.model.Film"%>
<%@page import="filmarsivi.dao.FavoriDao"%>
<%@page import="filmarsivi.model.Favori"%>
<%@page import="filmarsivi.dao.YorumDao"%>
<%@page import="filmarsivi.model.Yorum"%>
<%@page import="java.util.List"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>


<%
    request.setCharacterEncoding("UTF-8");
    int filmId = Integer.parseInt(request.getParameter("id"));

    FilmDao filmDao = new FilmDao();
    Film film = filmDao.findById(filmId);
    YorumBegeniDao begeniDao = new YorumBegeniDao();
    if (film == null) {
        out.print("Film bulunamad?.");
        return;
    }

    Kullanici kullanici = (Kullanici) session.getAttribute("kullanici");
    Integer kullaniciId = (Integer) session.getAttribute("kullanici_id");
    Boolean admin = (Boolean) session.getAttribute("admin");
    if (admin == null) admin = false;

    FavoriDao favoriDao = new FavoriDao();
    YorumDao yorumDao = new YorumDao();
    List<Yorum> yorumlar = yorumDao.listByFilmId(filmId);

    boolean favorideVar = false;
    if (kullaniciId != null) {
        favorideVar = favoriDao.isFavorited(kullaniciId, filmId);
    }

    String favoriEkleSil = request.getParameter("favoriEkleSil");
    if ("true".equals(favoriEkleSil)) {
    if (kullaniciId != null) {
        if (favorideVar) {
            favoriDao.delete(kullaniciId, filmId);
        } else {
            Favori favori = new Favori();
            favori.setFilmId(filmId);
            favori.setKullaniciId(kullaniciId);
            favoriDao.insert(favori);
        }
    }
    response.sendRedirect("FilmDetaysayfa.jsp?id=" + filmId);
    return;
}


   
       if ("POST".equalsIgnoreCase(request.getMethod())) {
    String yorumText = request.getParameter("yorumText");

    if (yorumText != null && !yorumText.trim().isEmpty()) {
        if (kullaniciId == null) {
            out.print("<script>alert('Yorum yapmak için giriş yapmalısınız.'); window.location.href='Kullanici/KullaniciGiris.jsp';</script>");
        } else {
            Yorum yeniYorum = new Yorum();
            yeniYorum.setFilmId(filmId);
            yeniYorum.setKullaniciId(kullaniciId);
            yeniYorum.setYorumText(yorumText);

            String tarih = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            yeniYorum.setYorumTarih(tarih);

            if (!yorumDao.insert(yeniYorum)) {
                out.print("Yorum eklenemedi.");
            } else {
                response.sendRedirect("FilmDetaysayfa.jsp?id=" + filmId);
                return;
            }
        }
    }
}

    String yorumSilId = request.getParameter("yorumSilId");
    if (yorumSilId != null && kullaniciId != null) {
        try {
            boolean isDeleted = yorumDao.delete(Integer.parseInt(yorumSilId));
            response.sendRedirect("FilmDetaysayfa.jsp?id=" + filmId);
                return;
        } catch (NumberFormatException e) {
            out.print("Geçersiz yorum ID.");
        }
    }
    String begeniYorumIdStr = request.getParameter("yorumBegeniId");

if (begeniYorumIdStr != null && kullaniciId != null) {
    try {
        int yorumId = Integer.parseInt(begeniYorumIdStr);

        // Kullanıcı bu yorumu daha önce beğenmiş mi?
        if (begeniDao.isLiked(yorumId,kullaniciId)) {
            // Zaten beğenmişse, beğeniyi kaldır
            begeniDao.delete(yorumId,kullaniciId);
        } else {
            // Beğenmemişse, önce UNIQUE ihlali olmasın diye tekrar kontrol et
            if (!begeniDao.isLiked(kullaniciId, yorumId)) {
                YorumBegeni begeni = new YorumBegeni();
                begeni.setKullaniciId(kullaniciId);
                begeni.setYorumId(yorumId);
                begeniDao.insert(begeni);
            }
        }

    } catch (NumberFormatException e) {
        out.println("<p style='color:red;'>⚠️ Geçersiz yorum ID formatı.</p>");
    } catch (Exception e) {
        out.println("<p style='color:red;'>❌ Beğeni işlemi sırasında bir hata oluştu.</p>");
    }

    // Sayfa yenileniyor
    response.sendRedirect("FilmDetaysayfa.jsp?id=" + filmId);
    return;
}
%>



<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Film Detay</title>
    <style>
        * {
    background-color: #FEE0DF;
}
h2{
    margin-left: 5%;
}
a {
    text-decoration: none;
    color: inherit;
}

/* ?frame'ler için stil */
.custom-iframe-top {
    border: none; 
    margin: 0;
    padding: 0;
    display: block;
    width: 100%;
    height: 300px;
    margin-bottom: 20px;
}

.custom-iframe-bottom {
    border: none; 
    margin: 0;
    padding: 0;
    display: block;
    width: 100%;
    height: 600px;
    margin-bottom: 20px;
}


.container {
    width: 100%;
    height: 50%;
    margin-top: 20px;
}


.tvFilmCikisYili {
    display: inline;
    width: 100%;
    height: 20px;
}


.filmDetayRow {
    display: flex;
    flex-direction: row;
    gap: 10px;
    justify-content: space-between;
    align-items: center;
    width: 80%;
}


.filmAciklamaColumn {
    width: 80%;
    height: auto;
    display: flex;
    flex-direction: column;
    align-items: start;
    gap: 30px;
}


.favoriyeEkleRow {
    display: flex;
    flex-direction: row;
    gap: 10px;
    justify-content: flex-start;
    align-items: center;
}


.filmResimFavoriColumn {
    width: 300px;
    height: auto;
    display: flex;
    flex-direction: column;
    align-items: start;
    gap: 10px;
    margin-left: 200px; 
}


.filmBaslikRow {
    height: 20px;
    width: 100%;
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
}


.adminButtonlar {
    height: auto;
    display: flex;
    flex-direction: column;
    align-items: start;
    gap: 10px;
    margin-left: 15px;
    padding: 50px;
}

.adminButtonlar input[type="submit"] {
    padding: 10px 20px;
    background-color: transparent;
    border: 2px solid gray;
    color: gray;
    font-size: 16px;
    border-radius: 25px;
    cursor: pointer;
    transition: all 0.3s ease;
    width: 200px;
}


.adminButtonlar input[type="submit"]:hover {
    background-color: #45a049;
    border-color: #45a049;
}


        .yorum-kutu {
            display: flex;
    justify-content: space-between;
    align-items: center; 
    margin-bottom: 10px;
    border: 1px solid #ccc;
    position: relative;
         margin-top: 20px;
    padding: 10px;
    background-color: white;
    border-radius: 5px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    margin-left: 50px;
    margin-right: 50px;
        }


    
        .silButon {
            position: absolute;
            top: 5px;
            right: 5px;
            cursor: pointer;
            color: red;
            font-weight: bold;
             cursor: pointer;
    background: none;
    border: none;
    font-size: 16px;
        }
        
        
        .resim-wrapper {
    width: 220px;
    height: 320px;
    padding: 10px;
    background-color: #ffe6f0;
    border: 4px solid #ff69b4; 
    border-radius: 15px;
    box-shadow: 0 4px 15px rgba(255, 105, 180, 0.3); 
    
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 20px auto;
}
        
   .video-wrapper {
    width: 640px;
    height: 480px;
    padding: 10px;
    background-color: #ffe6f0; 
    border: 4px solid #ff69b4; 
    border-radius: 15px;
    box-shadow: 0 4px 15px rgba(255, 105, 180, 0.3); 
    
    /* Ortalamak için */
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 30px 0;
}

    </style>
   
      
      <script>
    function confirmSilme() {
        return confirm("Bu filmi silmek istediğinize emin misiniz?");
    }
</script>


</head>
<body>
<jsp:include page="Ortak/Header.jsp" />

<hr/>

<div class="container">
    <div class="filmDetayRow">
        <div class="filmResimFavoriColumn">
           
            <div class="filmBaslikRow">
                <h3><%= film.getFilmBaslik() %></h3>
            </div>
            <div class="resim-wrapper">
    <img src="<%= film.getFilmResimUrl() %>" alt="Film Resmi" width="200" height="300"/>
</div>

 <div class="tvFilmCikisYili">Çıkış Yılı: <%= film.getFilmCikisYili() %></div>
            <div class="favoriyeEkleRow">
    <% if (kullaniciId != null) { %>
        <p style="margin: 0; font-size: 12px; color: gray;">
            <%= favoriDao.getFavoriSayisi(film.getId()) %> kişi favoriye ekledi
        </p>
        <img src="<%= (favorideVar ? "Resimler/favori_simgesi_filled.png" : "Resimler/favori_simgesi_empty.png") %>" 
             alt="Favori Ekle" class="favoriResmi"
             onclick="window.location.href='FilmDetaysayfa.jsp?id=<%= film.getId() %>&favoriEkleSil=true'" />
    <% } %>
</div>


            <p>Süre: <%= film.getFilmSure() %> dakika | Puan: <%= film.getFilmPuan() %></p>
        </div>

        <div class="filmAciklamaColumn">
    <div class="aciklamaVeButonlar" style="display: flex; flex-direction: row; align-items: flex-start; justify-content: space-between; width: 100%;">
        
        <%
    String videoUrl = film.getFilmUrl();


    String videoId = "dQw4w9WgXcQ";
    int index = videoUrl.indexOf("v=");
    if (index != -1) {
        videoId = videoUrl.substring(index + 2);
    }
%>



        <div style="flex: 1;margin-top: 50px;width:300px;">
            <p><%= film.getFilmAciklama() %></p>
        </div>
     
        <% if (admin) { %>
        <div class="adminButtonlar" style="display: flex; flex-direction: column; gap: 10px; margin-left: 20px;">
            <form action="Admin/FilmSil.jsp" method="post" onsubmit="return confirmSilme();">
    <input type="hidden" name="filmId" value="<%= film.getId() %>">
    <input type="submit" value="Filmi Sil">
</form>




            <form action="Admin/FilmGuncelle.jsp" method="get">
                <input type="hidden" name="filmId" value="<%= film.getId() %>">
                <input type="submit" value="Filmi Düzenle">
            </form>
        </div>
        <% } %>
          <div class="video-wrapper">
    <iframe width="620" height="460"
        src="https://www.youtube.com/embed/<%= videoId %>"
        title="YouTube video"
        frameborder="0"
        allowfullscreen>
    </iframe>
    </div>

    </div>
</div>
    </div>
</div>



<hr/>

<h2>Yorumlar</h2>
<div id="yorumlar">
    <% for (Yorum yorum : yorumlar) { 
        int begeniSayisi = begeniDao.countByYorumId(yorum.getId());
        boolean begenildiMi = false;
        if (kullaniciId != null) {
            begenildiMi = begeniDao.isLiked(yorum.getId(),kullaniciId);
        }
    %>
        <div class="yorum-kutu" id="yorum-<%= yorum.getId() %>">
            <div class="yorum-icerik" style="background-color:white;">
                <strong style="background-color:white;"><%= yorum.getKullaniciAdi() %></strong>
                <em style="background-color:white;"><%= yorum.getYorumTarih() %></em><br/>
                <%= yorum.getYorumText() %>
            </div>
            
            <% if ((kullaniciId != null && yorum.getKullaniciId() == kullaniciId) || admin) { %>
                <form method="post" class="silme-formu">
                    <input type="hidden" name="yorumSilId" value="<%= yorum.getId() %>">
                    <button type="submit" class="silButon">X</button>
                </form>
            <% } %>
               <div style="margin-right:30px;background-color: white;">
                       <form method="post" style="display: inline;background-color: white;">
                        <input type="hidden" name="yorumBegeniId" value="<%= yorum.getId() %>">
                        <button type="submit" style="border: none;background-color: white; cursor: pointer;">
                            <img src="<%= begenildiMi ? "like_filled.png" : "like_empty.png" %>" width="20"/>
                        </button>
                    </form>
                    <span style="background-color:white;"><%= begeniSayisi %> beğeni</span>
                </div>
        </div>
    <% } %>
</div>


<hr/>

<% if (kullaniciId != null) { %>
    <h2>Yorum Yap</h2>
    <form method="POST">
        <div class="comment-section">
            <textarea id="yorumText" name="yorumText" rows="5" cols="40" style="width: 1500px; margin-left: 100px;"></textarea><br/>
        </div>
        <button type="submit" style="margin-left: 100px;">Yorumu Gönder</button>
    </form>
<% } else { %>
    <p style="margin-left: 100px;">Yorum yapabilmek için giriş yapmalısınız.</p>
<% } %>

<br/>


</body>
</html>
