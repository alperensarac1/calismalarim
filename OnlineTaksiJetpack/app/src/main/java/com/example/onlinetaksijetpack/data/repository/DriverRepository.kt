package com.example.onlinetaksijetpack.data.repository

import com.example.onlinetaksijetpack.data.remote.api.ApiService
import com.example.onlinetaksijetpack.data.remote.model.*
import com.example.onlinetaksijetpack.util.Resource

class DriverRepository(
    private val apiService: ApiService
) {
    suspend fun updateLocation(lat: Double, lng: Double): Resource<Unit> {
        return try {
            val response = apiService.updateDriverLocation(
                DriverLocationUpdateRequest(lat, lng)
            )
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Konum gönderilemedi")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }

    suspend fun setOnline(isOnline: Boolean): Resource<DriverProfileResponse> {
        return try {
            val response = apiService.updateDriverOnlineStatus(
                DriverOnlineStatusRequest(isOnline)
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Online/offline güncellenemedi")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }

    suspend fun getAvailableRides(): Resource<AvailableRideListResponse> {
        return try {
            val response = apiService.getAvailableRides()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Ride listesi alınamadı")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }

    suspend fun acceptRide(rideId: Int): Resource<RideResponse> {
        return try {
            val response = apiService.acceptRide(rideId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Ride kabul edilemedi")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }

    suspend fun getMyActiveRides(): Resource<RideListResponse> {
        return try {
            val response = apiService.getMyActiveRides()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Aktif ride alınamadı")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }

    suspend fun updateRideStatus(
        rideId: Int,
        status: String,
        note: String? = null
    ): Resource<RideResponse> {
        return try {
            val response = apiService.updateRideStatus(
                rideId,
                UpdateRideStatusRequest(status, note)
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Status güncellenemedi")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Hata")
        }
    }
}