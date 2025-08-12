package com.example.sozlukkotlin.servis

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtils {
    const val BASE_URL = "https://alperensaracdeneme.com/sozluk/"

    fun getService(): SozlukApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SozlukApiService::class.java)
    }
}
