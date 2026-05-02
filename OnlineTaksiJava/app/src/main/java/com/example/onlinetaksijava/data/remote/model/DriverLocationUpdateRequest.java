package com.example.onlinetaksijava.data.remote.model;

public class DriverLocationUpdateRequest {
    private double lat;
    private double lng;

    public DriverLocationUpdateRequest(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
}
