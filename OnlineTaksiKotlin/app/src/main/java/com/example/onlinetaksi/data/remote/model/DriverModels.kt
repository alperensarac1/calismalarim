package com.example.onlinetaksi.data.remote.model


data class DriverLocationUpdateRequest(
    val lat: Double,
    val lng: Double
)

data class DriverProfileResponse(
    val user_id: Int,
    val is_online: Boolean,
    val current_lat: Double?,
    val current_lng: Double?
)