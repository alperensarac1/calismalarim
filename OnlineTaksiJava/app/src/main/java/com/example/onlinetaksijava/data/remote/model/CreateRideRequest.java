package com.example.onlinetaksijava.data.remote.model;


public class CreateRideRequest {
    private double pickup_lat;
    private double pickup_lng;
    private String pickup_address;
    private double dropoff_lat;
    private double dropoff_lng;
    private String dropoff_address;

    public CreateRideRequest(double pickup_lat, double pickup_lng, String pickup_address,
                             double dropoff_lat, double dropoff_lng, String dropoff_address) {
        this.pickup_lat = pickup_lat;
        this.pickup_lng = pickup_lng;
        this.pickup_address = pickup_address;
        this.dropoff_lat = dropoff_lat;
        this.dropoff_lng = dropoff_lng;
        this.dropoff_address = dropoff_address;
    }
}
