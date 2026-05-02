package com.example.onlinetaksijetpack.data.remote.model

data class CreateRideRequest(
    val pickup_lat: Double,
    val pickup_lng: Double,
    val pickup_address: String,
    val dropoff_lat: Double,
    val dropoff_lng: Double,
    val dropoff_address: String
)

data class RideResponse(
    val id: Int,
    val customer_id: Int,
    val assigned_driver_id: Int?,
    val pickup_lat: Double,
    val pickup_lng: Double,
    val pickup_address: String,
    val dropoff_lat: Double,
    val dropoff_lng: Double,
    val dropoff_address: String,
    val status: String,
    val estimated_fare: Double?,
    val final_fare: Double?,
    val cancel_reason: String?
)