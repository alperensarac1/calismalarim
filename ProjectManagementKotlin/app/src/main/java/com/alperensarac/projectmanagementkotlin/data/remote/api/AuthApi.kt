package com.alperensarac.projectmanagementkotlin.data.remote.api

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.AuthSessionDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.AuthUserDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.LoginRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.LogoutRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.RefreshTokenRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Authentication endpointlerini tanımlayan Retrofit servisidir.
 *
 * Base URL sonunda "/" bulunduğu için endpoint yollarını başında "/"
 * olmadan tanımlıyoruz.
 *
 * Base URL:
 * http://10.203.83.58:8080/
 *
 * Tam login adresi:
 * http://10.203.83.58:8080/api/Auth/login
 */
interface AuthApi {

    /**
     * Kullanıcı giriş işlemi.
     *
     * Bu endpoint Authorization header gerektirmez.
     */
    @POST("api/Auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): ApiResponse<AuthSessionDto>

    /**
     * Access token yenileme işlemi.
     *
     * Backend yalnızca refreshToken alanını beklemektedir.
     *
     * Dönen yeni refresh token eski tokenın yerine kaydedilmelidir.
     */
    @POST("api/Auth/refresh")
    suspend fun refresh(
        @Body request: RefreshTokenRequestDto
    ): ApiResponse<AuthSessionDto>

    /**
     * Kullanıcının mevcut cihazdaki refresh tokenını geçersiz kılar.
     *
     * Swagger üzerindeki kilit simgesine göre endpoint Bearer token ile
     * korunmaktadır. AuthInterceptor hazır olduğunda Authorization header
     * otomatik olarak eklenecektir.
     */
    @POST("api/Auth/logout")
    suspend fun logout(
        @Body request: LogoutRequestDto
    ): ApiResponse<Unit>

    /**
     * Oturum açmış kullanıcının güncel bilgilerini getirir.
     *
     * Authorization:
     * Bearer ACCESS_TOKEN
     */
    @GET("api/Auth/me")
    suspend fun getMe(): ApiResponse<AuthUserDto>
}