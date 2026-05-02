package com.example.onlinetaksi.data.remote.socket

import com.example.onlinetaksi.data.remote.model.DriverLocationEvent
import org.json.JSONObject

object SocketEventParser {

    fun getEventName(message: String): String? {
        return try {
            val jsonObject = JSONObject(message)
            jsonObject.optString("event")
        } catch (e: Exception) {
            null
        }
    }

    fun parseDriverLocation(message: String): DriverLocationEvent? {
        return try {
            val jsonObject = JSONObject(message)
            val eventName = jsonObject.optString("event")
            if (eventName != "DRIVER_LOCATION") return null

            val data = jsonObject.optJSONObject("data") ?: return null

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
            val jsonObject = JSONObject(message)
            val eventName = jsonObject.optString("event")

            if (eventName != "RIDE_STATUS_CHANGED" &&
                eventName != "RIDE_ACCEPTED" &&
                eventName != "RIDE_CANCELLED"
            ) {
                return null
            }

            val data = jsonObject.optJSONObject("data") ?: return null
            data.optString("status")
        } catch (e: Exception) {
            null
        }
    }
}