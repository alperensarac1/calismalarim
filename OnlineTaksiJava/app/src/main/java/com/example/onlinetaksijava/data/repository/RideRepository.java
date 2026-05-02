package com.example.onlinetaksijava.data.repository;


import com.example.onlinetaksijava.data.remote.api.ApiService;
import com.example.onlinetaksijava.data.remote.model.CreateRideRequest;
import com.example.onlinetaksijava.data.remote.model.RideResponse;

import retrofit2.Call;

public class RideRepository {

    private final ApiService apiService;

    public RideRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<RideResponse> createRide(CreateRideRequest request) {
        return apiService.createRide(request);
    }
}
