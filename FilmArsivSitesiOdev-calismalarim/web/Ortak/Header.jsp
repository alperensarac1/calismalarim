<%@page import="filmarsivi.model.Kullanici"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    Kullanici kullanici = (Kullanici) session.getAttribute("kullanici");
    Boolean admin = (Boolean) session.getAttribute("admin");
    
%>
<style>
    header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  height: 200px;
  padding: 0 20px;
  background-color: #FEE0DF;
  color: white;
  position: relative;
}

.header-buttons{
    display: flex;
    flex-direction: column;
    
}

.header-left img {
  width: 250px;
  height: 150px;
  cursor: pointer;
}

.header-center {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 60%; 
}

.header-button {
  padding: 10px 50px;
  background-color: transparent;
  border: 2px solid gray;
  color: gray;
  cursor: pointer;
  margin-inline: 20px;
  font-size: 16px;
  border-radius: 30px;
}

.header-button:hover {
  background-color: #45a049;
}

.header-button.down {
  margin-top: 40px;
}

.search-bar {
  padding: 10px;
  font-size: 16px;
  border-radius: 30px;
  border: 2px solid gray;
  width: 400px;
  background-color: transparent;
  color: gray;
}

.search-bar::placeholder {
  color: gray;
}


.header-right {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-right: 50px;
  }
  
  .hamburger-icon {
    font-size: 50px;
    color: gray;
    cursor: pointer;
  }
.dropdown-menu-container {
    display: none;
    position: absolute;
    top: 60px;
    right: 20px;
    background-color: #fff;
    border: 1px solid #ccc;
    border-radius: 8px;
    z-index: 1000;
    width: 220px;
}

.dropdown-menu, .dropdown-menu ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.dropdown-menu > li {
    padding: 12px;
    border-bottom: 1px solid #eee;
    background-color: #fff;
    cursor: pointer;
    position: relative;
}

.dropdown-menu > li:hover {
    background-color: #f5f5f5;
}

.dropdown-menu li ul {
    display: none;
    background-color: #f9f9f9;
    position: relative;
    left: 0;
}

.dropdown-menu li:hover > ul {
    display: block;
}

.dropdown-menu li ul li {
    padding: 10px;
    border-top: 1px solid #ddd;
}

.dropdown-menu li ul li a {
    text-decoration: none;
    color: #333;
    display: block;
}

.dropdown-menu li ul li:hover {
    background-color: #eaeaea;
}

  .left-button {
    padding: 10px 20px;
    background-color: transparent;
    border: 2px solid gray;
    color: gray;
    cursor: pointer;
    font-size: 16px;
    border-radius: 30px;
  }
  
  .left-button:hover {
    background-color: #FF4500;
  }
  
</style>
<%
    String baseURL = request.getContextPath(); // Kök dizini alır
%>

<header>
    <div class="header-left">
        <a href="<%= baseURL %>/FilmlerAnasayfa.jsp">
            <img src="<%= baseURL %>/site-icon.png" alt="Logo" class="clickable-image">
        </a>
    </div>

    <div class="header-center">
        <div class="header-buttons">
            <div>
                <a href="<%= baseURL %>/Filmler2025Sonrasi.jsp">
                    <button class="header-button up">2025 Filmleri</button>
                </a>
                <a href="<%= baseURL %>/FilmlerTop100.jsp">
                    <button class="header-button up">Top 100</button>
                </a>
            </div>
            <a href="<%= baseURL %>/FilmlerEnCokBegenilenler.jsp">
                <button class="header-button down">En Çok Beğenilenler</button>
            </a>
        </div>
        <form action="FilmAra.jsp" method="get" style="display: flex; align-items: center;">
    <input type="text" name="filmBaslik" class="search-bar" placeholder="Ara...">
    <button type="submit" style="display: none;"></button> <!-- Enter ile çalışsın diye -->
</form>

    </div>

    <div class="header-right">
        <% boolean favoriAktif = (kullanici != null); %>

        <% if (favoriAktif) { %>
            <!-- Giriş yapıldıysa buton aktif ve tıklanabilir -->
           <a href="<%= baseURL %>/Favoriler.jsp">
                <button class="header-button left-button">Favoriler</button>
            </a>
            <% } else { %>
            <!-- Giriş yapılmadıysa pasif (disabled) buton -->
            <button class="header-button left-button" disabled>Favoriler</button>
<% } %>


        <div class="hamburger-icon" onclick="toggleMenu()">&#9776;</div>

        <div class="dropdown-menu-container" id="dropdownMenu">
            <ul class="dropdown-menu">
                <% if (kullanici == null) { %>
                    <li><a href="<%= baseURL %>/Kullanici/KullaniciGiris.jsp" style="color:black;">Giriş Yap</a></li>
                    <li><a href="<%= baseURL %>/Kullanici/KullaniciKayit.jsp" style="color:black;">Üye Ol</a></li>
                <% } else { %>
                    <li><a href="<%= baseURL %>/Kullanici/KullaniciCikis.jsp" style="color:black;">Çıkış Yap</a></li>
                    <% if (admin != null && admin) { %>
                        <li>
                            Admin İşlemleri
                            <ul>
                                <li><a href="<%= baseURL %>/Admin/ResimEkle.jsp">Resim Ekle</a></li>
                                <li><a href="<%= baseURL %>/Admin/FilmEkle.jsp">Film Ekle</a></li>
                                <li><a href="<%= baseURL %>/Admin/KategoriEkle.jsp">Kategori Ekle</a></li>
                                <li><a href="<%= baseURL %>/Admin/AdminIslemleri.jsp">Yönetim</a></li>
                            </ul>
                        </li>
                    <% } %>
                <% } %>
            </ul>
        </div>
    </div>
</header>


<script>
    function toggleMenu() {
        const menu = document.getElementById('dropdownMenu');
        menu.style.display = (menu.style.display === 'block') ? 'none' : 'block';
    }

    document.addEventListener('click', function (event) {
        const menu = document.getElementById('dropdownMenu');
        const icon = document.querySelector('.hamburger-icon');
        if (!menu.contains(event.target) && !icon.contains(event.target)) {
            menu.style.display = 'none';
        }
    });
</script>
