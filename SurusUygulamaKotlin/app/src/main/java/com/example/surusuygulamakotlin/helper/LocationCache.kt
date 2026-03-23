package com.example.surusuygulamakotlin.helper


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

data class DeviceLocation(
    val lat: Double,
    val lng: Double,
    val acc: Float,
    val at: String // "yyyy-MM-dd HH:mm:ss"
)

object LocationCache {

    private const val PREFS = "rec_prefs"
    private const val K_LAT = "last_lat"
    private const val K_LNG = "last_lng"
    private const val K_ACC = "last_acc"
    private const val K_AT = "last_loc_at"
    private const val K_AT_MS = "last_loc_at_ms"

    fun hasPermission(ctx: Context): Boolean {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun readIfFresh(ctx: Context, maxAgeMs: Long): DeviceLocation? {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val atMs = p.getLong(K_AT_MS, 0L)
        if (atMs <= 0L) return null
        val age = System.currentTimeMillis() - atMs
        if (age > maxAgeMs) return null

        val lat = java.lang.Double.longBitsToDouble(p.getLong(K_LAT, 0L))
        val lng = java.lang.Double.longBitsToDouble(p.getLong(K_LNG, 0L))
        val acc = p.getFloat(K_ACC, -1f)
        val at = p.getString(K_AT, null) ?: return null
        if (acc < 0f) return null

        return DeviceLocation(lat, lng, acc, at)
    }

    fun save(ctx: Context, loc: DeviceLocation) {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit()
            .putLong(K_LAT, java.lang.Double.doubleToRawLongBits(loc.lat))
            .putLong(K_LNG, java.lang.Double.doubleToRawLongBits(loc.lng))
            .putFloat(K_ACC, loc.acc)
            .putString(K_AT, loc.at)
            .putLong(K_AT_MS, System.currentTimeMillis())
            .apply()
    }

    @SuppressLint("MissingPermission")
    suspend fun getMandatory(ctx: Context, nowStr: () -> String, freshMaxAgeMs: Long = 60_000L): DeviceLocation {
        if (!hasPermission(ctx)) throw IllegalStateException("Konum izni yok")

        // 1) Cache tazeyse kullan
        readIfFresh(ctx, freshMaxAgeMs)?.let { return it }

        // 2) Taze konum al
        val fused = LocationServices.getFusedLocationProviderClient(ctx)
        val token = CancellationTokenSource()

        val loc = withContext(Dispatchers.IO) {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token).awaitCompat()
        } ?: throw IllegalStateException("Konum alınamadı")

        val out = DeviceLocation(
            lat = loc.latitude,
            lng = loc.longitude,
            acc = loc.accuracy,
            at = nowStr()
        )
        save(ctx, out)
        return out
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitCompat(): T {
        return suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) {} }
            addOnFailureListener { cont.resumeWith(Result.failure(it)) }
            addOnCanceledListener { cont.cancel() }
        }
    }
}
