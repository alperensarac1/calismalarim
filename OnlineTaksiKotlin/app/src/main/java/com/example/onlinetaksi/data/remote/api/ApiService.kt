package com.example.onlinetaksi.data.remote.api

import com.example.onlinetaksi.data.remote.model.AuthResponse
import com.example.onlinetaksi.data.remote.model.AvailableRideListResponse
import com.example.onlinetaksi.data.remote.model.CreateRideRequest
import com.example.onlinetaksi.data.remote.model.DriverLocationUpdateRequest
import com.example.onlinetaksi.data.remote.model.DriverProfileResponse
import com.example.onlinetaksi.data.remote.model.LoginRequest
import com.example.onlinetaksi.data.remote.model.RegisterRequest
import com.example.onlinetaksi.data.remote.model.RideListResponse
import com.example.onlinetaksi.data.remote.model.RideResponse
import com.example.onlinetaksi.data.remote.model.UpdateRideStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("customer/rides")
    suspend fun createRide(
        @Body request: CreateRideRequest
    ): Response<RideResponse>
    @PUT("driver/location")
    suspend fun updateDriverLocation(
        @Body request: DriverLocationUpdateRequest
    ): Response<DriverProfileResponse>
    @GET("driver/available-rides")
    suspend fun getAvailableRides(): Response<AvailableRideListResponse>

    @PUT("driver/rides/{rideId}/accept")
    suspend fun acceptRide(
        @Path("rideId") rideId: Int
    ): Response<RideResponse>

    @GET("driver/rides/my-active")
    suspend fun getMyActiveRides(): Response<RideListResponse>
    @PUT("driver/online")
    suspend fun setDriverOnline(): Response<Unit>

    @PUT("driver/offline")
    suspend fun setDriverOffline(): Response<Unit>

    @PUT("driver/rides/{rideId}/status")
    suspend fun updateRideStatus(
        @Path("rideId") rideId: Int,
        @Body request: UpdateRideStatusRequest
    ): Response<RideResponse>
}