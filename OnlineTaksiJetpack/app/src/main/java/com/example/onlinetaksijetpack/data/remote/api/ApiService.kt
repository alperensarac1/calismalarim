package com.example.onlinetaksijetpack.data.remote.api


import com.example.onlinetaksijetpack.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("customer/rides")
    suspend fun createRide(@Body request: CreateRideRequest): Response<RideResponse>

    @PUT("driver/location")
    suspend fun updateDriverLocation(
        @Body request: DriverLocationUpdateRequest
    ): Response<DriverProfileResponse>

    @PUT("driver/online-status")
    suspend fun updateDriverOnlineStatus(
        @Body request: DriverOnlineStatusRequest
    ): Response<DriverProfileResponse>

    @GET("driver/available-rides")
    suspend fun getAvailableRides(): Response<AvailableRideListResponse>

    @PUT("driver/rides/{rideId}/accept")
    suspend fun acceptRide(@Path("rideId") rideId: Int): Response<RideResponse>

    @GET("driver/rides/my-active")
    suspend fun getMyActiveRides(): Response<RideListResponse>

    @PUT("driver/rides/{rideId}/status")
    suspend fun updateRideStatus(
        @Path("rideId") rideId: Int,
        @Body request: UpdateRideStatusRequest
    ): Response<RideResponse>
}