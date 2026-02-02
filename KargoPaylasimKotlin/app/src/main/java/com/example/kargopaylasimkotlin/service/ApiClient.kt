import android.content.Context
import android.util.Log
import com.example.kargopaylasimkotlin.service.CargoApi
import com.example.kargopaylasimkotlin.service.TokenStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://alperensaracdeneme.com/cargo/"
    private const val HEADER_TOKEN = "X-Auth-Token"   // <-- WAF dostu

    fun createApi(context: Context): CargoApi {
        val tokenStore = TokenStore(context)

        val authInterceptor = Interceptor { chain ->
            val token = tokenStore.getToken()
            val req = chain.request()

            if (!token.isNullOrBlank()) {
                Log.d("AUTH_TOKEN", token.take(12) + "...")
            } else {
                Log.d("AUTH_TOKEN", "NULL/EMPTY")
            }

            val newReq = if (!token.isNullOrBlank()) {
                req.newBuilder()
                    // Authorization bazı hostinglerde bloklanabiliyor
                    .removeHeader("Authorization")
                    .removeHeader(HEADER_TOKEN)
                    .addHeader(HEADER_TOKEN, token) // <-- Bearer yok, direkt token
                    .build()
            } else req

            chain.proceed(newReq)
        }

        val logging = HttpLoggingInterceptor().apply {
            // İstersen HEADERS yap (BODY çok uzun/çok hassas veri basabilir)
            level = HttpLoggingInterceptor.Level.BODY
        }

        val urlLogger = Interceptor { chain ->
            val req = chain.request()
            Log.d("HTTP_URL", "${req.method} ${req.url}")
            chain.proceed(req)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(urlLogger)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(CargoApi::class.java)
    }
}
