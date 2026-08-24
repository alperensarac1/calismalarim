package com.alperensarac.projectmanagementkotlin.data.remote.api

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.AuthSessionDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.RefreshTokenRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Yalnızca token yenileme işlemi için kullanılan Retrofit servisidir.
 *
 * Bu servis AuthInterceptor veya TokenAuthenticator içermeyen ayrı bir
 * OkHttpClient üzerinden çalışır.
 *
 * Böylece refresh endpoint'i 401 döndürürse authenticator tekrar tetiklenmez
 * ve sonsuz yenileme döngüsü oluşmaz.
 */
interface RefreshApi {

    @POST("api/Auth/refresh")
    suspend fun refresh(
        @Body request: RefreshTokenRequestDto
    ): ApiResponse<AuthSessionDto>
}