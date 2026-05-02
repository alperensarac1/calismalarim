package com.example.onlinetaksi.data.repository

import com.example.onlinetaksi.data.remote.api.ApiService
import com.example.onlinetaksi.data.remote.model.CreateRideRequest
import com.example.onlinetaksi.data.remote.model.RideResponse
import com.example.onlinetaksi.util.Resource

class RideRepository(
    private val apiService: ApiService
) {
    suspend fun createRide(request: CreateRideRequest): Resource<RideResponse> {
        return try {
            val response = apiService.createRide(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Ride oluşturulamadı")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Beklenmeyen hata oluştu")
        }
    }

}