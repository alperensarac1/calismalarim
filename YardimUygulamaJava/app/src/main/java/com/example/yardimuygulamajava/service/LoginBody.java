package com.example.yardimuygulamajava.service;


public class LoginBody {
    public String telefon;
    public String sifre;

    public LoginBody(String telefon, String sifre) {
        this.telefon = telefon;
        this.sifre = sifre;
    }
}
