package com.example.qryoklamajetpack.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority


object LocationHelper {

    fun hasLocationPermission(ctx: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    fun isLocationEnabled(ctx: Context): Boolean {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) {
            true
        }
    }

    fun showEnableLocationDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Konum Kapalı")
            .setMessage("Konum servisleri kapalı görünüyor. Açmak ister misiniz?")
            .setPositiveButton("Aç") { _, _ ->
                activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    /** Java'daki gibi: null olmamalı, 0,0 olmamalı, 2 dk'dan eski olmamalı, accuracy 100m’den kötü olmamalı */
    fun isLocationUsable(loc: Location?): Boolean {
        if (loc == null) return false
        if (loc.latitude == 0.0 && loc.longitude == 0.0) return false

        val ageMs = System.currentTimeMillis() - loc.time
        if (ageMs > 120_000) { // 2 dk
            Log.w("LOC_CHECK", "Konum çok eski, ageMs=$ageMs")
            return false
        }

        if (loc.hasAccuracy() && loc.accuracy > 100f) {
            Log.w("LOC_CHECK", "Konum doğruluğu kötü, acc=${loc.accuracy}")
            return false
        }

        return true
    }

    fun getCurrentLocation(
        activity: Activity,
        fused: FusedLocationProviderClient,
        onLoc: (Double, Double) -> Unit,
        onFail: (String) -> Unit = { msg ->
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
        }
    ) {
        if (!hasLocationPermission(activity)) {
            onFail("Konum izni gerekli")
            return
        }

        if (!isLocationEnabled(activity)) {
            showEnableLocationDialog(activity)
            onFail("Konum kapalı")
            return
        }

        try {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (isLocationUsable(loc)) {
                        onLoc(loc.latitude, loc.longitude)
                    } else {
                        fused.lastLocation
                            .addOnSuccessListener { last ->
                                if (isLocationUsable(last)) {
                                    onLoc(last.latitude, last.longitude)
                                } else {
                                    onFail("Konum alınamadı veya çok eski.\nGPS'i açıp birkaç saniye bekleyin.")
                                }
                            }
                            .addOnFailureListener { e ->
                                onFail("Konum (last) hatası: ${e.message}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    onFail("Konum hatası: ${e.message}")
                }
        } catch (se: SecurityException) {
            onFail("Konum izni yok")
        }
    }
}