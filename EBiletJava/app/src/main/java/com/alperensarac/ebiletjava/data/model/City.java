package com.alperensarac.ebiletjava.data.model;

public class City {

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /*
        Spinner içinde şehir adının görünmesi için
        toString() metodunu name dönecek şekilde ayarlıyoruz.

        Böylece Spinner adapter'a City listesi verirsek
        ekranda otomatik şehir adı görünür.
    */
    @Override
    public String toString() {
        return name;
    }
}
