package com.example.eticaretkotlin.repo

import com.example.eticaretkotlin.service.AuthApi
import com.example.eticaretkotlin.service.ProductApi
import com.example.eticaretkotlin.service.CartApi
import com.example.eticaretkotlin.service.OrderApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    private const val BASE_URL = "https://alperensaracdeneme.com/eticaret/api/" // sondaki / şart

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val productApi: ProductApi = retrofit.create(ProductApi::class.java)
    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val cartApi: CartApi = retrofit.create(CartApi::class.java)
    val orderApi: OrderApi = retrofit.create(OrderApi::class.java)
}
