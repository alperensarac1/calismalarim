<%@page import="filmarsivi.dao.FilmDao"%>
<%@ page import="filmarsivi.dao.FilmDao" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    // Admin kontrolü
    Boolean adminMi = (Boolean) session.getAttribute("admin");
    if (adminMi == null || !adminMi) {
        response.sendRedirect("../FilmlerAnasayfa.jsp");
        return;
    }

    // Parametre al
    String filmIdStr = request.getParameter("filmId");

    if (filmIdStr != null) {
        try {
            int filmId = Integer.parseInt(filmIdStr);

            FilmDao filmDao = new FilmDao();
            boolean basarili = filmDao.delete(filmId);

            if (basarili) {
                response.sendRedirect("../FilmlerAnasayfa.jsp");
            } else {
                out.println("<h3 style='color:red;'>❌ Film silinemedi.</h3>");
                out.println("<a href='FilmDetaysayfa.jsp?id=" + filmId + "'>Geri Dön</a>");
            }
        } catch (NumberFormatException e) {
            out.println("<h3 style='color:red;'>⚠️ Geçersiz film ID.</h3>");
        }
    } else {
        out.println("<h3 style='color:red;'>⚠️ Film ID bulunamadı.</h3>");
    }
%>

