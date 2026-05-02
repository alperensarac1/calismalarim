package com.example.onlinetaksi.ui.driver

import com.example.onlinetaksi.data.remote.model.AvailableRideItem
import com.example.onlinetaksi.data.remote.model.RideResponse

data class DriverHomeUiState(
    val availableRides: List<AvailableRideItem> = emptyList(),
    val activeRide: RideResponse? = null,
    val currentLat: String = "-",
    val currentLng: String = "-",
    val lastLog: String = "Hazır"
)