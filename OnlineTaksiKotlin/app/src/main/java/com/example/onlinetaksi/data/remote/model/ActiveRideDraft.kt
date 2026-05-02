package com.example.onlinetaksi.data.remote.model

data class ActiveRideDraft(
    val pickupLat: Double,
    val pickupLng: Double,
    val pickupAddress: String,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val dropoffAddress: String
)