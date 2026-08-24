package com.alperensarac.projectmanagementauthenticator.data.remote.api

import com.alperensarac.projectmanagementauthenticator.data.remote.model.ApiResponse
import com.alperensarac.projectmanagementauthenticator.data.remote.model.LoginRequest
import com.alperensarac.projectmanagementauthenticator.data.remote.model.LoginResponseData
import com.alperensarac.projectmanagementauthenticator.data.remote.model.LoginUserData

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


/*
 * =========================================================
 * MAIN BACKEND API
 * =========================================================
 */


/**
 * Mevcut .NET ProjectManagement API endpointlerini
 * Retrofit üzerinden temsil eder.
 *
 * Bu interface içerisinde yalnızca endpoint
 * tanımlamaları bulunur.
 *
 * Burada:
 *
 * - OkHttpClient oluşturulmaz.
 * - Hata yönetimi yapılmaz.
 * - DataStore işlemi yapılmaz.
 * - Ekran durumu yönetilmez.
 *
 * Bu sorumluluklar ilerleyen aşamalarda ayrı sınıflara
 * dağıtılacaktır.
 */
interface MainBackendApi {

    /*
     * =====================================================
     * LOGIN
     * =====================================================
     */


    /**
     * Kullanıcının mevcut ProjectManagement hesabıyla
     * giriş yapmasını sağlar.
     *
     * İstek adresi:
     *
     * POST /api/Auth/login
     *
     * Gönderilen JSON:
     *
     * {
     *   "email": "kullanici@example.com",
     *   "password": "kullanici-sifresi"
     * }
     *
     * Retrofit bu fonksiyon suspend olduğu için isteği
     * coroutine içerisinde asenkron olarak çalıştırır.
     *
     * Response<T> kullanmamızın nedeni:
     *
     * - HTTP durum kodunu okuyabilmek
     * - Başarısız response body değerini inceleyebilmek
     * - 401, 400 ve 500 gibi cevapları yönetebilmek
     */
    @POST("api/Auth/login")
    suspend fun login(
        @Body
        request: LoginRequest,
    ): Response<ApiResponse<LoginResponseData>>


    /*
     * =====================================================
     * AKTİF KULLANICI
     * =====================================================
     */


    /**
     * Access tokenın geçerli olup olmadığını kontrol eder
     * ve token sahibinin güncel kullanıcı bilgilerini
     * getirir.
     *
     * İstek adresi:
     *
     * GET /api/Auth/me
     *
     * Authorization başlığı örneği:
     *
     * Authorization: Bearer eyJhbGciOi...
     *
     * Bu endpoint şu amaçlarla kullanılacaktır:
     *
     * - Login sonrasında kullanıcı bilgisini doğrulamak
     * - Uygulama yeniden açıldığında oturumu kontrol etmek
     * - Kullanıcı ID, e-posta ve görünen adı almak
     * - Süresi dolmuş tokenı tespit etmek
     */
    @GET("api/Auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization")
        authorizationHeader: String,
    ): Response<ApiResponse<LoginUserData>>
}