package com.example.eticaretkotlin.service

import com.example.eticaretkotlin.model.ApiResponse
import com.example.eticaretkotlin.model.LoginRequest
import com.example.eticaretkotlin.model.LoginResponse
import com.example.eticaretkotlin.model.RegisterRequest
import com.example.eticaretkotlin.model.RegisterResponse
import com.example.eticaretkotlin.model.UserDto
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

