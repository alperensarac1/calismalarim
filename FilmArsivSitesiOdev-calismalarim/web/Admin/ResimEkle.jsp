<%@page import="java.util.List"%>
<%@page import="filmarsivi.dao.FilmDao"%>
<%@ page import="java.io.*, javax.servlet.http.Part" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    request.setCharacterEncoding("UTF-8");

    String yuklenenYol = "";
    int silinen = 0;

    String action = request.getParameter("action");

    if ("upload".equals(action)) {
        InputStream input = null;
        FileOutputStream fos = null;
        try {
            Part filePart = request.getPart("resim");
            if (filePart != null && filePart.getSize() > 0) {
                String dosyaAdi = filePart.getSubmittedFileName();
                String uzanti = dosyaAdi.substring(dosyaAdi.lastIndexOf("."));
                String yeniDosyaAdi = System.currentTimeMillis() + uzanti;

                String kayitYolu = application.getRealPath("/Resimler");
                File klasor = new File(kayitYolu);
                if (!klasor.exists()) klasor.mkdirs();

                File hedefDosya = new File(klasor, yeniDosyaAdi);
                input = filePart.getInputStream();
                fos = new FileOutputStream(hedefDosya);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }

                yuklenenYol = "Resimler/" + yeniDosyaAdi;
            }
        } catch (Exception e) {
            out.println("<p style='color:red;'>Hata: " + e.getMessage() + "</p>");
        } finally {
            if (fos != null) try { fos.close(); } catch (Exception ignore) {}
            if (input != null) try { input.close(); } catch (Exception ignore) {}
        }
    } else if ("clean".equals(action)) {
        FilmDao filmDao = new FilmDao();
        List kullanilanResimler = filmDao.getAllResimYollari();

        String klasorYolu = application.getRealPath("/Resimler");
        File resimKlasoru = new File(klasorYolu);

        if (resimKlasoru.exists()) {
            File[] dosyalar = resimKlasoru.listFiles();
            for (int i = 0; i < dosyalar.length; i++) {
                File dosya = dosyalar[i];
                String dosyaAdi = "Resimler/" + dosya.getName();
                if (!kullanilanResimler.contains(dosyaAdi)) {
                    if (dosya.delete()) {
                        silinen++;
                    }
                }
            }
        }
    }
%>

<!DOCTYPE html>
<html>
<head>
    
    <meta charset="UTF-8">
    <title>Resim Yönetimi</title>
    <style>
        body {
            background-color: #fce3e3;
            font-family: Arial, sans-serif;
            display: flex;
            justify-content: center;
            align-items: flex-start;
            min-height: 100vh;
            margin: 0;
            padding-top: 50px;
        }

        .kutu {
            background-color: white;
            padding: 30px;
            margin-top: 400px;
            border-radius: 12px;
            box-shadow: 0 0 30px rgba(0, 0, 0, 0.1);
            width: 400px;
            text-align: center;
        }

        input[type="file"],
        input[type="submit"] {
            margin: 10px 0;
            padding: 10px 15px;
            font-size: 14px;
            border-radius: 8px;
            border: 1px solid #ccc;
        }

        input[type="submit"] {
            background-color: #d94d4d;
            color: white;
            border: none;
            cursor: pointer;
            width: 120px;
        }

        input[type="submit"]:hover {
            background-color: #b13e3e;
        }

        img {
            margin-top: 15px;
            width: 200px;
            border-radius: 10px;
        }

        .info {
            margin-top: 15px;
        }
    </style>
</head>
<body>
    <div style="position: fixed; top: 0; left: 0; width: 100%; z-index: 999; background-color: white;">
    <jsp:include page="../Ortak/Header.jsp" />
</div>
<div class="kutu">
    <h2>Resim Yönetimi</h2>

    <form method="POST" enctype="multipart/form-data">
        <input type="hidden" name="action" value="upload">
        <input type="file" name="resim" accept="image/*" required><br/>
        <input type="submit" value="Yükle">
    </form>

    <form method="POST">
        <input type="hidden" name="action" value="clean">
        <input type="submit" value="Temizle">
    </form>

    <% if (!yuklenenYol.equals("")) { %>
    <div class="info">
        <p>
            Yüklenen Dosya: 
            <span id="dosyaYolu"><%= yuklenenYol %></span>
            <button onclick="kopyalaPanoya()" id="kopyaBtn" title="Kopyala" style="border:none; background:none; cursor:pointer;">
                📋
            </button>
        </p>
        <img src="../<%= yuklenenYol %>" width="200">
    </div>

    <script>
        function kopyalaPanoya() {
            const yol = document.getElementById("dosyaYolu").innerText;
            navigator.clipboard.writeText(yol).then(function() {
                const btn = document.getElementById("kopyaBtn");
                btn.innerText = "✅";
                btn.title = "Kopyalandı!";
                setTimeout(() => {
                    btn.innerText = "📋";
                    btn.title = "Kopyala";
                }, 1500);
            });
        }
    </script>
<% } %>

    <% if (silinen > 0) { %>
        <p class="info"><%= silinen %> kullanılmayan resim silindi.</p>
    <% } else if ("clean".equals(action)) { %>
        <p class="info">Silinecek kullanılmayan resim bulunamadı.</p>
    <% } %>
</div>
</body>
</html>
