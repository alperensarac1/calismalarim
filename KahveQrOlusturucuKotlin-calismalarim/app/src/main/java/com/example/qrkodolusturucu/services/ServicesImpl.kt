package com.example.qrkodolusturucu.services

import com.example.qrkodolusturucu.services.dummyservice.KahveDummyServisDao
import com.example.qrkodolusturucu.services.retrofitservice.KahveHTTPServisDao

class ServicesImpl{
    fun getInstance():Services = KahveHTTPServisDao()
}