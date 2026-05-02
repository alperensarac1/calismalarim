package com.example.onlinetaksijetpack.data.repository


import com.example.onlinetaksijetpack.data.remote.api.ApiService
import com.example.onlinetaksijetpack.data.remote.model.AuthResponse
import com.example.onlinetaksijetpack.data.remote.model.LoginRequest
import com.example.onlinetaksijetpack.data.remote.model.RegisterRequest
import com.example.onlinetaksijetpack.util.Resource

class AuthRepository(
    private val apiService: ApiService
) {
    suspend fun login(request: LoginRequest): Resource<AuthResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Giriş başarısız")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }

    suspend fun register(request: RegisterRequest): Resource<AuthResponse> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Kayıt başarısız")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }
}