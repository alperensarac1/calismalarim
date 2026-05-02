package com.example.onlinetaksi.data.repository

import com.example.onlinetaksi.data.remote.api.ApiService
import com.example.onlinetaksi.data.remote.model.LoginRequest
import com.example.onlinetaksi.data.remote.model.RegisterRequest
import com.example.onlinetaksi.util.Resource

class AuthRepository(
    private val apiService: ApiService
) {

    suspend fun register(request: RegisterRequest): Resource<com.example.onlinetaksi.data.remote.model.AuthResponse> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Kayıt başarısız.")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Beklenmeyen hata oluştu.")
        }
    }

    suspend fun login(request: LoginRequest): Resource<com.example.onlinetaksi.data.remote.model.AuthResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Giriş başarısız.")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Beklenmeyen hata oluştu.")
        }
    }
}