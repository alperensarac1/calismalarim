package com.example.qrkodolusturucujetpack.services.retrofitservice

class ApiUtils {
    companion object {
        val BASE_URL = "https://alperensaracdeneme.com/kahveservis/"
        fun getKahveHTTPServisDaoInterface():KahveHTTPServisDaoInterface{
            return RetrofitClient.getRetrofitClient(BASE_URL).create(KahveHTTPServisDaoInterface::class.java)
        }
    }
}