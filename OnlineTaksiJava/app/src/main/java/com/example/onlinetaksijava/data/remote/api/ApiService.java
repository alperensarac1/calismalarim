package com.example.onlinetaksijava.data.remote.api;



import com.example.onlinetaksijava.data.remote.model.AuthResponse;
import com.example.onlinetaksijava.data.remote.model.AvailableRideListResponse;
import com.example.onlinetaksijava.data.remote.model.CreateRideRequest;
import com.example.onlinetaksijava.data.remote.model.DriverLocationUpdateRequest;
import com.example.onlinetaksijava.data.remote.model.DriverOnlineStatusRequest;
import com.example.onlinetaksijava.data.remote.model.DriverProfileResponse;
import com.example.onlinetaksijava.data.remote.model.LoginRequest;
import com.example.onlinetaksijava.data.remote.model.RegisterRequest;
import com.example.onlinetaksijava.data.remote.model.RideListResponse;
import com.example.onlinetaksijava.data.remote.model.RideResponse;
import com.example.onlinetaksijava.data.remote.model.UpdateRideStatusRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("customer/rides")
    Call<RideResponse> createRide(@Body CreateRideRequest request);

    @PUT("driver/location")
    Call<DriverProfileResponse> updateDriverLocation(@Body DriverLocationUpdateRequest request);

    @PUT("driver/online-status")
    Call<DriverProfileResponse> updateDriverOnlineStatus(@Body DriverOnlineStatusRequest request);

    @GET("driver/available-rides")
    Call<AvailableRideListResponse> getAvailableRides();

    @PUT("driver/rides/{rideId}/accept")
    Call<RideResponse> acceptRide(@Path("rideId") int rideId);

    @GET("driver/rides/my-active")
    Call<RideListResponse> getMyActiveRides();

    @PUT("driver/rides/{rideId}/status")
    Call<RideResponse> updateRideStatus(
            @Path("rideId") int rideId,
            @Body UpdateRideStatusRequest request
    );
}
