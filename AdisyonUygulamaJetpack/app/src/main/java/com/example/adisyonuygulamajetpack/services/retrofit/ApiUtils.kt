package com.example.adisyonuygulamakotlin.services.retrofit

class ApiUtils {
    companion object {
        val BASE_URL = "https://alperensaracdeneme.com/adisyon/"
        fun getAdisyonServisDaoInterface():AdisyonServiceInterface{
            return RetrofitClient.getRetrofitClient(BASE_URL).create(AdisyonServiceInterface::class.java)
        }
    }
}