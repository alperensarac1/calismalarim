package com.example.onlinetaksijava.data.remote.model;

public class UpdateRideStatusRequest {
    private String status;
    private String note;

    public UpdateRideStatusRequest(String status, String note) {
        this.status = status;
        this.note = note;
    }
}

