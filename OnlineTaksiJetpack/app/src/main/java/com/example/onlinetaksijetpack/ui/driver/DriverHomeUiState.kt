package com.example.onlinetaksijetpack.ui.driver


import com.example.onlinetaksijetpack.data.remote.model.AvailableRideItem
import com.example.onlinetaksijetpack.data.remote.model.RideResponse

data class DriverHomeUiState(
    val isOnline: Boolean = false,
    val currentLat: String = "-",
    val currentLng: String = "-",
    val availableRides: List<AvailableRideItem> = emptyList(),
    val activeRide: RideResponse? = null,
    val isLoadingAvailableRides: Boolean = false,
    val isAcceptingRide: Boolean = false,
    val lastLog: String = "Hazır",
    val message: String? = null
)