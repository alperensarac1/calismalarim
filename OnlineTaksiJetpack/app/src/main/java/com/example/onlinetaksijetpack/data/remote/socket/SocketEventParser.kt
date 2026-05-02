package com.example.onlinetaksijetpack.data.remote.socket


import com.example.onlinetaksijetpack.data.remote.model.DriverLocationEvent
import org.json.JSONObject

object SocketEventParser {

    fun getEventName(message: String): String? {
        return try {
            val json = JSONObject(message)
            json.optString("event")
        } catch (e: Exception) {
            null
        }
    }

    fun parseDriverLocation(message: String): DriverLocationEvent? {
        return try {
            val json = JSONObject(message)
            val event = json.optString("event")
            if (event != "DRIVER_LOCATION") return null

            val data = json.optJSONObject("data") ?: return null

            DriverLocationEvent(
                rideId = data.optInt("ride_id", -1),
                driverId = data.optInt("driver_id", -1),
                lat = data.optDouble("lat", 0.0),
                lng = data.optDouble("lng", 0.0)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseRideStatus(message: String): String? {
        return try {
            val json = JSONObject(message)
            val event = json.optString("event")

            if (event != "RIDE_STATUS_CHANGED" &&
                event != "RIDE_ACCEPTED" &&
                event != "RIDE_CANCELLED"
            ) {
                return null
            }

            val data = json.optJSONObject("data") ?: return null
            data.optString("status")
        } catch (e: Exception) {
            null
        }
    }
}