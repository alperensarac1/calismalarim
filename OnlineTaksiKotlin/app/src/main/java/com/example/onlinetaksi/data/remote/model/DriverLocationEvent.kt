package com.example.onlinetaksi.data.remote.model

data class DriverLocationEvent(
    val rideId: Int,
    val driverId: Int,
    val lat: Double,
    val lng: Double
)