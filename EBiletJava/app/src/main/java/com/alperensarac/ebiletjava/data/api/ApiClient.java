package com.alperensarac.ebiletjava.data.api;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    /*
        Backend ana URL.

        DİKKAT:
        Sonda / olmak zorunda.

        Doğru:
        http://10.0.2.2/event_ticket_api/

        Yanlış:
        http://10.0.2.2/event_ticket_api
    */
    private static final String BASE_URL = "https://alperensaracdeneme.com/event_ticket_api/";

    /*
        Retrofit nesnesini tek sefer oluşturup tüm projede kullanacağız.
    */
    private static Retrofit retrofit = null;

    /*
        ApiService nesnesini döndürür.

        Kullanım:
        ApiClient.getApiService().login(...)
    */
    public static ApiService getApiService() {

        /*
            Eğer retrofit daha önce oluşturulmadıysa oluştur.
            Oluşturulduysa mevcut nesneyi kullan.
        */
        if (retrofit == null) {

            /*
                Logging Interceptor:
                API istek ve cevaplarını Logcat'te görmemizi sağlar.

                Geliştirme aşamasında çok faydalıdır.
                Örneğin:
                - Hangi URL'ye istek atıldı?
                - Hangi POST verileri gönderildi?
                - Sunucu ne cevap verdi?
            */
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            /*
                OkHttpClient:
                Retrofit'in alt tarafta kullandığı HTTP istemcisidir.
            */
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            /*
                Retrofit oluşturma.
            */
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(ApiService.class);
    }

    /*
        Bazı adapterlarda poster URL oluştururken base URL gerekebilir.
        Bunun için public getter ekliyoruz.
    */
    public static String getBaseUrl() {
        return BASE_URL;
    }
}
