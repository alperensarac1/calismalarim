package com.example.qrkodolusturucujava.model;

public enum UrunKategori {
    INDIRIMLI,
    ATISTIRMALIKLAR,
    ICECEKLER;

    public int kategoriKodu() {
        switch (this) {
            case INDIRIMLI:
                return 0;
            case ATISTIRMALIKLAR:
                return 1;
            case ICECEKLER:
                return 2;
            default:
                return -1;
        }
    }
}

