package com.example.onlinetaksijava.data.remote.model;

public class RideResponse {
    private int id;
    private int customer_id;
    private Integer assigned_driver_id;
    private double pickup_lat;
    private double pickup_lng;
    private String pickup_address;
    private double dropoff_lat;
    private double dropoff_lng;
    private String dropoff_address;
    private String status;
    private Double estimated_fare;
    private Double final_fare;
    private String cancel_reason;

    public int getId() {
        return id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public Integer getAssigned_driver_id() {
        return assigned_driver_id;
    }

    public double getPickup_lat() {
        return pickup_lat;
    }

    public double getPickup_lng() {
        return pickup_lng;
    }

    public String getPickup_address() {
        return pickup_address;
    }

    public double getDropoff_lat() {
        return dropoff_lat;
    }

    public double getDropoff_lng() {
        return dropoff_lng;
    }

    public String getDropoff_address() {
        return dropoff_address;
    }

    public String getStatus() {
        return status;
    }

    public Double getEstimated_fare() {
        return estimated_fare;
    }

    public Double getFinal_fare() {
        return final_fare;
    }

    public String getCancel_reason() {
        return cancel_reason;
    }
}
