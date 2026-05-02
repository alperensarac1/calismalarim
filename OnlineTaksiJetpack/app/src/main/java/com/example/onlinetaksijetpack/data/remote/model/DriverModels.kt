package com.example.onlinetaksijetpack.data.remote.model

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

data class AvailableRideItem(
    val id: Int,
    val customer_id: Int,
    val pickup_lat: Double,
    val pickup_lng: Double,
    val pickup_address: String,
    val dropoff_lat: Double,
    val dropoff_lng: Double,
    val dropoff_address: String,
    val status: String,
    val estimated_fare: Double?
)

data class AvailableRideListResponse(
    val rides: List<AvailableRideItem>
)

data class RideListResponse(
    val rides: List<RideResponse>
)

data class DriverOnlineStatusRequest(
    val is_online: Boolean
)

data class UpdateRideStatusRequest(
    val status: String,
    val note: String? = null
)