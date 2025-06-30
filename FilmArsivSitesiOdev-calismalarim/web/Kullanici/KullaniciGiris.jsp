<%-- 
    Document   : KullaniciGiris
    Created on : 27 Nis 2025, 15:13:12
    Author     : alperensarac
--%>
<%@page import="filmarsivi.utils.Sifreleme"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@page import="filmarsivi.dao.KullaniciDao"%>
<%@page import="filmarsivi.dao.AdminDao"%>
<%@page import="filmarsivi.model.Kullanici"%>
<%
    String login = request.getParameter("login");
    String message = "";

    if (login != null) {
        String kullaniciAdi = request.getParameter("kullaniciAdi");
        String kullaniciSifre = request.getParameter("kullaniciSifre");

        
        String hashlenmisSifre = Sifreleme.sha256Hash(kullaniciSifre);

        KullaniciDao kullaniciDao = new KullaniciDao();
        Kullanici kullanici = kullaniciDao.findByUsername(kullaniciAdi);

        if (kullanici != null) {
            if (kullanici.getKullaniciSifre().equals(hashlenmisSifre)) {
                session.setAttribute("kullanici_id", kullanici.getId());
                session.setAttribute("kullanici_adi", kullanici.getKullaniciAdi());

                AdminDao adminDao = new AdminDao();
                boolean adminMi = adminDao.isAdmin(kullanici.getId());

                session.setAttribute("kullanici", kullanici); 
                session.setAttribute("admin", adminMi);       
                response.sendRedirect("../FilmlerAnasayfa.jsp");

                return;
            } else {
                message = "Şifre yanlış!";
            }
        } else {
            message = "Kullanıcı bulunamadı!";
        }
    }

    request.setAttribute("message", message);
%>




<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login / Register</title>
        <style>
            body {
    position: relative;
    
    background-color: #FEE0DF;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    font-family: 'Inter';
    font-style: normal;
}
.login-container {
    background-color: #FEE0DF;
    padding: 20px;
    padding-bottom: 80px;
    padding-top: 10px;
    border: 2px solid gray;
    box-shadow: 0 0 10px rgba(10, 10, 10, 0.1);
    border-radius: 8px;
    height: 500px;
    width: 600px;
    text-align: center;
    justify-content: center;

}

.login-container h1 {
    font-size: 60px;
    
    color: gray;
    font-family: 'Inter';
font-style: normal;
font-weight: 700;
font-size: 30px;
line-height: 36px;
}
.login-container input[type="email"],
.login-container input[type="password"] {
    width: 70%;
    padding: 10px;
    margin-bottom: 15px;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-sizing: border-box;
font-family: 'Inter';
font-style: normal;
font-weight: 400;
font-size: 14px;
line-height: 17px;


color: rgba(0, 0, 0, 0.1);
}
.login-container input[type="checkbox"] {
    margin-right: 10px;
}
.login-container button {
    width: 70%;
    padding: 10px;
    font-family: 'Inter';
font-style: normal;
font-size: 16px;
line-height: 19px;
    color: white;
    border: none;
    border: 1px solid rgba(0, 0, 0, 0.3);
border-radius: 3px;
    margin-top: 5%;
    cursor: pointer;
    
    
}

.login-container a {
    display: block;
    margin-top: 20px;
    
    font-family: 'Inter';
font-style: normal;
font-size: 13px;
line-height: 12px;

color: #9C346B;


}
.login-container .forgot-password {
    position: absolute;
    text-align: right;
    
    margin-left: 22%;

}
.login-container .remember-me {
    display: flex;
    align-items: center;
    justify-content: left;
    margin-left: 40%;
    margin-top: 20px;
    margin-bottom: 10px;
    font-family: 'Inter';
font-style: normal;
font-weight: 400;
font-size: 13px;
line-height: 12px;

color: #9C346B;

}
.login-container .description-text{
    display: block;
    margin-left: 15%;
    text-align: left;
    font-family: 'Inter';
    font-family: 'Inter';
font-style: normal;
font-size: 16px;
line-height: 19px;

color: rgba(0, 0, 0, 0.6);

    
}
.sign-in-btn{
    background: #9C346B;
    border-radius: 5px;
}
.Main{
    font-family: Arial, sans-serif;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    margin: 0;
    margin-top: 3%;
    
}
.SpaceBox{
    width: 100px;
}

.girisButton input[type="submit"] {
    padding: 15px 30px; 
    background-color: white; 
    color: gray; 
    font-size: 18px;
    border: 2px solid gray;
    border-radius: 60px;
    cursor: pointer;
    width: 40%;
    margin-top: 20px;
    transition: all 0.3s ease;
}

.girisButton input[type="submit"]:hover {
    background-color: #45a049;
    border-color: #45a049;
}

.Main {
    font-family: Arial, sans-serif;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    margin: 0;
}


.contText {
    font-family: 'Inter';
    font-style: normal;
    font-weight: 400;
    font-size: 16px;
    line-height: 19px;
    color: #FFFFFF;
}


.description-text {
    display: block;
    margin-left: 15%;
    text-align: left;
    font-family: 'Inter';
    font-style: normal;
    font-size: 16px;
    line-height: 19px;
    color: rgba(0, 0, 0, 0.6);
}


.login-container input[type="text"],
.login-container input[type="password"] {
    padding: 20px;
    font-size: 16px;
    text-decoration: underline;
    border-radius: 60px; 
    border: 2px solid gray;
    width: 400px; 
    background-color: white; 
    color: gray;
}


.etEmail::placeholder {
    color: gray; 
}


  

        </style>

</head>
<body>
<jsp:include page="../Ortak/Header.jsp" />
<hr/>
 <div class="Main">
    <div class="login-container">
        <h1>Giriş</h1>
        <form method="post">
            <div class="description-text">
                <p>Kullanıcı Adınız</p>
            </div>
            <input type="text" name="kullaniciAdi" placeholder="Kullanıcı Adınızı Giriniz" class="etEmail" required>
            
            <div class="description-text">
                <p>Şifreniz</p>
            </div>
            <input type="password" name="kullaniciSifre" placeholder="******" class="etEmail" required>
            
            <div class="forgot-password">
                <a href="#">Şifremi Unuttum</a>
            </div>

            <div class="girisButton">
                <input type="submit" name="login" value="Giriş Yap">
            </div>

            <div class="remember-me">
                <input type="checkbox" name="rememberMe"> <label>Beni Hatırla</label>
            </div>
        </form>

        <div class="error-message">
            <%= request.getAttribute("message") != null ? request.getAttribute("message") : "" %> 
        </div>
    </div>
    
    
</div>

</body>
</html>

