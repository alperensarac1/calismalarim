<%@page import="filmarsivi.dao.KullaniciDao"%>
<%@page import="filmarsivi.dao.AdminDao"%>
<%@page import="filmarsivi.model.Kullanici"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>

<%
    Boolean admin = (Boolean) session.getAttribute("admin");
    if (admin == null || !admin) {
        response.sendRedirect("../FilmlerAnasayfa.jsp");
        return;
    }

    String mesaj = "";
    String arama = request.getParameter("arama");

    KullaniciDao kullaniciDao = new KullaniciDao();
    AdminDao adminDao = new AdminDao();
    List<Kullanici> bulunanlar = null;

    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String silId = request.getParameter("silId");
        String adminYapId = request.getParameter("adminYapId");

        if (silId != null) {
            boolean silindi = kullaniciDao.delete(Integer.parseInt(silId));
            mesaj = silindi ? "Kullanıcı silindi." : "Silme başarısız!";
        } else if (adminYapId != null) {
            boolean eklendi = adminDao.insert(Integer.parseInt(adminYapId));
            mesaj = eklendi ? "Kullanıcı admin yapıldı." : "Zaten admin olabilir veya işlem başarısız!";
        }
    }

    if (arama != null && !arama.trim().isEmpty()) {
        bulunanlar = kullaniciDao.searchByName(arama);
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin - Kullanıcı İşlemleri</title>
</head>
<body style="font-family: Arial; background-color: #FEE0DF;">
    <jsp:include page="../Ortak/Header.jsp" />

    <h2>Kullanıcı Ara</h2>
    <form method="get">
        <input type="text" name="arama" placeholder="Kullanıcı adı girin" value="<%= arama != null ? arama : "" %>">
        <input type="submit" value="Ara">
    </form>

    <p style="color: green;"><%= mesaj %></p>

    <% if (bulunanlar != null) { %>
        <table border="1" cellpadding="10" style="margin-top: 20px; background-color: white;">
            <tr>
                <th>ID</th>
                <th>Kullanıcı Adı</th>
                <th>Kayıt Tarihi</th>
                <th>İşlemler</th>
            </tr>
            <% for (Kullanici k : bulunanlar) { %>
                <tr>
                    <td><%= k.getId() %></td>
                    <td><%= k.getKullaniciAdi() %></td>
                    <td><%= k.getKullaniciKayitTarihi() %></td>
                    <td>
                        <form method="post" style="display:inline;">
                            <input type="hidden" name="silId" value="<%= k.getId() %>">
                            <input type="submit" value="Sil">
                        </form>

                        <form method="post" style="display:inline;">
                            <input type="hidden" name="adminYapId" value="<%= k.getId() %>">
                            <input type="submit" value="Admin Yap">
                        </form>
                    </td>
                </tr>
            <% } %>
        </table>
    <% } else if (arama != null) { %>
        <p style="color:red;">Kullanıcı bulunamadı.</p>
    <% } %>
</body>
</html>

