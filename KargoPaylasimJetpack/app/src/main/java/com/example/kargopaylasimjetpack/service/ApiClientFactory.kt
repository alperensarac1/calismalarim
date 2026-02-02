package com.example.kargopaylasimjetpack.service


import com.example.kargopaylasimjetpack.storage.TokenStore
import com.example.kargopaylasimjetpack.util.AppConfig
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClientFactory {

    fun create(tokenStore: TokenStore): ApiService {

        val authInterceptor = Interceptor { chain ->
            val req = chain.request()
            val token = runBlocking { tokenStore.getTokenFast() }
            val newReq = if (!token.isNullOrBlank()) {
                req.newBuilder()
                    .addHeader("X-Auth-Token", token)
                    .addHeader("Content-Type", "application/json")
                    .build()
            } else {
                req.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
            }
            chain.proceed(newReq)
        }

        val log = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val ok = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(log)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(ok)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
