package com.example.onlinetaksijava.data.remote.model;


public class DriverLocationEvent {
    private final int rideId;
    private final int driverId;
    private final double lat;
    private final double lng;

    public DriverLocationEvent(int rideId, int driverId, double lat, double lng) {
        this.rideId = rideId;
        this.driverId = driverId;
        this.lat = lat;
        this.lng = lng;
    }

    public int getRideId() {
        return rideId;
    }

    public int getDriverId() {
        return driverId;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
