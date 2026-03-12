package com.example.yardimuygulamajava.service;


public class RegisterBody {
    public String role;
    public String ad;
    public String soyad;
    public Integer yas;
    public String telefon;
    public String il;
    public String ilce;
    public String sifre;

    public RegisterBody(String role, String ad, String soyad, Integer yas,
                        String telefon, String il, String ilce, String sifre) {
        this.role = role;
        this.ad = ad;
        this.soyad = soyad;
        this.yas = yas;
        this.telefon = telefon;
        this.il = il;
        this.ilce = ilce;
        this.sifre = sifre;
    }
}
