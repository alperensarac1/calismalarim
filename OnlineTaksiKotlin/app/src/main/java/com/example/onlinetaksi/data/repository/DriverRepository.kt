package com.example.onlinetaksi.data.repository

import com.example.onlinetaksi.data.remote.api.ApiService
import com.example.onlinetaksi.data.remote.model.AvailableRideListResponse
import com.example.onlinetaksi.data.remote.model.DriverLocationUpdateRequest
import com.example.onlinetaksi.data.remote.model.RideListResponse
import com.example.onlinetaksi.data.remote.model.RideResponse
import com.example.onlinetaksi.data.remote.model.UpdateRideStatusRequest
import com.example.onlinetaksi.util.Resource

class DriverRepository(
    private val api: ApiService
) {

    suspend fun updateLocation(lat: Double, lng: Double): Resource<Unit> {
        return try {
            val response = api.updateDriverLocation(
                DriverLocationUpdateRequest(lat, lng)
            )
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Konum gönderilemedi")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }

    suspend fun getAvailableRides(): Resource<AvailableRideListResponse> {
        return try {
            val response = api.getAvailableRides()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Ride listesi alınamadı")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }

    suspend fun acceptRide(rideId: Int): Resource<RideResponse> {
        return try {
            val response = api.acceptRide(rideId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Ride kabul edilemedi")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }

    suspend fun getMyActiveRides(): Resource<RideListResponse> {
        return try {
            val response = api.getMyActiveRides()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Aktif ride alınamadı")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }
    suspend fun setOnline(): Resource<Unit> {
        return try {
            val res = api.setDriverOnline()
            if (res.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Online olunamadı")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }

    suspend fun setOffline(): Resource<Unit> {
        return try {
            val res = api.setDriverOffline()
            if (res.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Offline olunamadı")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }

    suspend fun updateRideStatus(rideId: Int, status: String): Resource<RideResponse> {
        return try {
            val res = api.updateRideStatus(
                rideId,
                UpdateRideStatusRequest(status)
            )
            if (res.isSuccessful && res.body() != null) {
                Resource.Success(res.body()!!)
            } else {
                Resource.Error("Status güncellenemedi")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }
}