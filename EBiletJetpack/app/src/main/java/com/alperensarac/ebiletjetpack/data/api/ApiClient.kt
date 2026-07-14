package com.alperensarac.ebiletjetpack.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*
    ApiClient

    Retrofit nesnesini tek yerden üretir.

    Retrofit:
    - PHP backend'e istek atmamızı sağlar.
    - ApiService interface'ini gerçek HTTP çağrılarına çevirir.
    - Gson converter ile JSON cevaplarını Kotlin data class'larına dönüştürür.

    Emulator için localhost:
    http://10.0.2.2/event_ticket_api/

    Gerçek telefonda test:
    http://BILGISAYAR_IP_ADRESIN/event_ticket_api/
*/
object ApiClient {

    /*
        Backend ana URL.

        DİKKAT:
        Sonda / olmak zorunda.
    */
    const val BASE_URL: String = "https://alperensaracdeneme.com/event_ticket_api/"

    /*
        Logcat'te API istek/cevap detaylarını görmek için.
    */
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /*
        Retrofit'in kullandığı HTTP client.
    */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /*
        Retrofit nesnesi.
    */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /*
        API servis arayüzü.
    */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}