package com.example.eticaretkotlin.test


import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// ------------------------------
// API Test DTO'ları
// ------------------------------
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class ApiResponse<T>(
    val ok: Boolean,
    val data: T?,
    val error: String?,
    val debug: Any? = null
)

data class RegisterResponse(
    val token: String,
    val user_id: Int
)

// ------------------------------
// Retrofit Test Arayüzü
// ------------------------------
interface AuthApiTest {

    @POST("auth.php")
    suspend fun register(
        @Query("action") action: String = "register",
        @Body body: RegisterRequest
    ): ApiResponse<RegisterResponse>
}

// ------------------------------
// Test Runner (tamamen bağımsız)
// ------------------------------
object PhpApiTester {

    private const val TAG = "PhpApiTester"

    private const val BASE_URL = "https://alperensaracdeneme.com/eticaret/api/"

    private val retrofit by lazy {

        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL) // DİKKAT: / ile bitmeli
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private val api: AuthApiTest by lazy { retrofit.create(AuthApiTest::class.java) }

    /**
     *  REGISTER TEST
     */
    fun testRegister() {
        Thread { // Thread çünkü Activity/Fragment istemiyoruz
            try {

                Log.d(TAG, ">>> TEST BAŞLADI: REGISTER")

                val req = RegisterRequest(
                    name = "TestUser",
                    email = "test_${System.currentTimeMillis()}@mail.com",
                    password = "123456"
                )

                CoroutineScope(Dispatchers.IO).launch{
                    val res = api.register(body = req)

                    Log.d(TAG, ">>> OK yanıtı: $res")
                }

            } catch (e: HttpException) {
                val code = e.code()
                val errBody = e.response()?.errorBody()?.string()
                Log.e(TAG, ">>> HTTP ERROR $code")
                Log.e(TAG, ">>> Sunucu cevabı: $errBody")
            } catch (e: Exception) {
                Log.e(TAG, ">>> UNKNOWN ERROR: ${e.message}", e)
            }
        }.start()
    }
}
