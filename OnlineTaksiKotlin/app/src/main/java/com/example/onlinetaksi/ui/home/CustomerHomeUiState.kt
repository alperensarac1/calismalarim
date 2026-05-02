package com.example.onlinetaksi.ui.home

data class CustomerHomeUiState(
    val socketConnected: Boolean = false,
    val lastSocketEvent: String = "Henüz event yok",
    val rideStatus: String = "Aktif ride bilgisi yok",
    val driverLatText: String = "-",
    val driverLngText: String = "-",
    val lastLocationUpdateText: String = "Henüz konum güncellemesi yok"
)