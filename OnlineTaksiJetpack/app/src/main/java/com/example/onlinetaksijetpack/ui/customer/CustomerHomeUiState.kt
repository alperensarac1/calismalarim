package com.example.onlinetaksijetpack.ui.customer

data class CustomerHomeUiState(
    val socketConnected: Boolean = false,
    val rideStatus: String = "Aktif ride bilgisi yok",
    val lastSocketEvent: String = "Henüz event yok",
    val driverLatText: String = "-",
    val driverLngText: String = "-",
    val lastLocationUpdateText: String = "Henüz konum güncellemesi yok",
    val isCreatingRide: Boolean = false,
    val message: String? = null,

    val pickupLat: Double? = null,
    val pickupLng: Double? = null,
    val dropoffLat: Double? = null,
    val dropoffLng: Double? = null,
    val driverLat: Double? = null,
    val driverLng: Double? = null
)