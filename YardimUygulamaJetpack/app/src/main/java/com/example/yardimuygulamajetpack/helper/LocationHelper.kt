package com.example.yardimuygulamajetpack.helper

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLatLng(ctx: Context): Pair<Double, Double>? {
        val client = LocationServices.getFusedLocationProviderClient(ctx)
        return suspendCancellableCoroutine { cont ->
            val token = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token)
                .addOnSuccessListener { loc ->
                    cont.resume(if (loc != null) (loc.latitude to loc.longitude) else null)
                }
                .addOnFailureListener { cont.resume(null) }
        }
    }

    fun reverseCityDistrict(ctx: Context, lat: Double, lng: Double): Pair<String?, String?> {
        return try {
            val g = Geocoder(ctx, Locale("tr","TR"))
            val a = g.getFromLocation(lat, lng, 1)?.firstOrNull()
            val city = a?.adminArea
            val district = a?.subAdminArea ?: a?.locality
            city to district
        } catch (_: Exception) {
            null to null
        }
    }
}