package com.alperensarac.ebiletkotlin.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*
    ApiClient

    Bu sınıf Retrofit nesnesini oluşturur.

    Retrofit ne işe yarar?
    - Android uygulama ile PHP backend arasında bağlantı kurar.
    - POST istekleri göndermemizi sağlar.
    - JSON cevaplarını Kotlin model sınıflarına çevirir.

    Bu projede PHP backend URL yapısı şöyle olacak:

    http://10.0.2.2/event_ticket_api/

    Örnek API:
    http://10.0.2.2/event_ticket_api/auth/login.php
*/
object ApiClient {

    /*
        Emulator kullanıyorsan:
        10.0.2.2 bilgisayarındaki localhost anlamına gelir.

        XAMPP / Laragon / WAMP üzerinde çalışıyorsan:
        http://10.0.2.2/event_ticket_api/

        Gerçek telefonda test edeceksen:
        http://BILGISAYAR_IP_ADRESIN/event_ticket_api/

        Örnek:
        http://192.168.1.35/event_ticket_api/
    */
    private const val BASE_URL = "https://alperensaracdeneme.com/event_ticket_api/"

    /*
        Logging Interceptor:
        API istek ve cevaplarını Logcat'te gösterir.

        Geliştirme aşamasında:
        - Hangi URL'ye istek atıldı?
        - Hangi POST değerleri gitti?
        - Backend ne cevap verdi?

        gibi şeyleri görmemizi sağlar.
    */
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /*
        OkHttpClient:
        Retrofit'in alt tarafta kullandığı HTTP istemcisidir.
    */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /*
        Retrofit nesnesi.
        lazy kullandık çünkü uygulama açılır açılmaz değil,
        ilk ihtiyaç olduğunda oluşturulsun.
    */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /*
        ApiService arayüzünü üretir.

        Kullanım:
        ApiClient.apiService.login(...)
    */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}