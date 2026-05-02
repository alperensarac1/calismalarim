package com.example.onlinetaksijava.data.remote.socket;

import com.example.onlinetaksijava.data.remote.model.DriverLocationEvent;

import org.json.JSONObject;

public class SocketEventParser {

    public static String getEventName(String message) {
        try {
            JSONObject obj = new JSONObject(message);
            return obj.optString("event");
        } catch (Exception e) {
            return null;
        }
    }

    public static DriverLocationEvent parseDriverLocation(String message) {
        try {
            JSONObject obj = new JSONObject(message);
            String event = obj.optString("event");
            if (!"DRIVER_LOCATION".equals(event)) return null;

            JSONObject data = obj.optJSONObject("data");
            if (data == null) return null;

            return new DriverLocationEvent(
                    data.optInt("ride_id", -1),
                    data.optInt("driver_id", -1),
                    data.optDouble("lat", 0.0),
                    data.optDouble("lng", 0.0)
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static String parseRideStatus(String message) {
        try {
            JSONObject obj = new JSONObject(message);
            String event = obj.optString("event");

            if (!"RIDE_STATUS_CHANGED".equals(event)
                    && !"RIDE_ACCEPTED".equals(event)
                    && !"RIDE_CANCELLED".equals(event)) {
                return null;
            }

            JSONObject data = obj.optJSONObject("data");
            if (data == null) return null;

            return data.optString("status");
        } catch (Exception e) {
            return null;
        }
    }
}
