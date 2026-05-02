package com.example.onlinetaksijava.data.remote.model;


public class DriverProfileResponse {
    private int user_id;
    private boolean is_online;
    private Double current_lat;
    private Double current_lng;

    public int getUser_id() {
        return user_id;
    }

    public boolean isIs_online() {
        return is_online;
    }

    public Double getCurrent_lat() {
        return current_lat;
    }

    public Double getCurrent_lng() {
        return current_lng;
    }
}
