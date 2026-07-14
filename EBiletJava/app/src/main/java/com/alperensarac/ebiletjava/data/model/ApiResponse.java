package com.alperensarac.ebiletjava.data.model;


/*
    ApiResponse.java

    PHP backend'deki bütün cevaplarımız şu formata sahip:

    Başarılı:
    {
        "success": true,
        "message": "Giriş başarılı",
        "data": {...}
    }

    Hatalı:
    {
        "success": false,
        "message": "E-posta veya şifre hatalı"
    }

    T generic tiptir.
    Yani data bazen User, bazen List<City>, bazen Ticket olabilir.
*/
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    /*
        Backend bazı hatalarda extra döndürebiliyor.
        Şimdilik Object olarak tutuyoruz.
    */
    private Object extra;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Object getExtra() {
        return extra;
    }
}
