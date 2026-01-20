package com.example.eticaretjetpack.service

import com.example.eticaretjetpack.model.ApiResponse
import com.example.eticaretjetpack.model.LoginRequest
import com.example.eticaretjetpack.model.LoginResponse
import com.example.eticaretjetpack.model.RegisterRequest
import com.example.eticaretjetpack.model.RegisterResponse
import com.example.eticaretjetpack.model.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<RegisterResponse>

    @GET("me")
    suspend fun me(@Header("Authorization") auth: String): ApiResponse<UserDto>
}

