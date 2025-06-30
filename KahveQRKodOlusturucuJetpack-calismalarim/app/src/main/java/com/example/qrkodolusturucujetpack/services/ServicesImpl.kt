package com.example.qrkodolusturucu.services

import com.example.qrkodolusturucujetpack.services.dummyservice.KahveDummyServisDao
import com.example.qrkodolusturucujetpack.services.retrofitservice.KahveHTTPServisDao

class ServicesImpl{
    fun getInstance():Services = KahveHTTPServisDao()
}