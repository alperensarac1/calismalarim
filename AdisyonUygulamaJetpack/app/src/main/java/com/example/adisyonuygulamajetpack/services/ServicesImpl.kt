package com.example.qrkodolusturucu.services

import com.example.adisyonuygulamakotlin.services.retrofit.AdisyonServisDao


class ServicesImpl{
    companion object{
        fun getInstance():Services = AdisyonServisDao()
    }

}