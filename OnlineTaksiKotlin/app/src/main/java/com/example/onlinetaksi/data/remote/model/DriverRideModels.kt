package com.example.onlinetaksi.data.remote.model

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