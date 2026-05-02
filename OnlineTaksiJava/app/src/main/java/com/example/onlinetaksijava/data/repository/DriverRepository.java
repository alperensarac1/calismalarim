package com.example.onlinetaksijava.data.repository;

import com.example.onlinetaksijava.data.remote.api.ApiService;
import com.example.onlinetaksijava.data.remote.model.AvailableRideListResponse;
import com.example.onlinetaksijava.data.remote.model.DriverLocationUpdateRequest;
import com.example.onlinetaksijava.data.remote.model.DriverOnlineStatusRequest;
import com.example.onlinetaksijava.data.remote.model.DriverProfileResponse;
import com.example.onlinetaksijava.data.remote.model.RideListResponse;
import com.example.onlinetaksijava.data.remote.model.RideResponse;
import com.example.onlinetaksijava.data.remote.model.UpdateRideStatusRequest;

import retrofit2.Call;

public class DriverRepository {

    private final ApiService apiService;

    public DriverRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<DriverProfileResponse> updateLocation(double lat, double lng) {
        return apiService.updateDriverLocation(new DriverLocationUpdateRequest(lat, lng));
    }

    public Call<DriverProfileResponse> setOnline(boolean isOnline) {
        return apiService.updateDriverOnlineStatus(new DriverOnlineStatusRequest(isOnline));
    }

    public Call<AvailableRideListResponse> getAvailableRides() {
        return apiService.getAvailableRides();
    }

    public Call<RideResponse> acceptRide(int rideId) {
        return apiService.acceptRide(rideId);
    }

    public Call<RideListResponse> getMyActiveRides() {
        return apiService.getMyActiveRides();
    }

    public Call<RideResponse> updateRideStatus(int rideId, String status, String note) {
        return apiService.updateRideStatus(rideId, new UpdateRideStatusRequest(status, note));
    }
}

