package com.example.onlinetaksijetpack.ui.customer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun CustomerMapSection(
    pickupLat: Double?,
    pickupLng: Double?,
    dropoffLat: Double?,
    dropoffLng: Double?,
    driverLat: Double?,
    driverLng: Double?
) {
    val defaultIstanbul = LatLng(41.0082, 28.9784)
    val cameraPositionState: CameraPositionState = rememberCameraPositionState()

    val pickupLatLng = if (pickupLat != null && pickupLng != null) LatLng(pickupLat, pickupLng) else null
    val dropoffLatLng = if (dropoffLat != null && dropoffLng != null) LatLng(dropoffLat, dropoffLng) else null
    val driverLatLng = if (driverLat != null && driverLng != null) LatLng(driverLat, driverLng) else null

    LaunchedEffect(pickupLatLng, dropoffLatLng, driverLatLng) {
        val points = listOfNotNull(pickupLatLng, dropoffLatLng, driverLatLng)

        if (points.isEmpty()) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(defaultIstanbul, 11f),
                durationMs = 800
            )
        } else if (points.size == 1) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(points.first(), 15f),
                durationMs = 800
            )
        } else {
            val builder = LatLngBounds.Builder()
            points.forEach { builder.include(it) }

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(builder.build(), 160),
                durationMs = 900
            )
        }
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        cameraPositionState = cameraPositionState
    ) {
        pickupLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Alınış Noktası",
                snippet = "Pickup",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }

        dropoffLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Varış Noktası",
                snippet = "Dropoff",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }

        driverLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Taksiniz",
                snippet = "Driver",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            )
        }
    }
}