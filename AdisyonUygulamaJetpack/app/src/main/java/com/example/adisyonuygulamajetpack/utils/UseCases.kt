package com.example.adisyonuygulamakotlin.utils

import com.example.adisyonuygulamakotlin.model.Urun


fun Float.fiyatYaz(): String {
    return String.format("%.2f ₺", this)
}
