package com.example.eticaretjava.service;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

import com.example.eticaretjava.model.Util.ApiResponse;
import com.example.eticaretjava.model.User.LoginRequest;
import com.example.eticaretjava.model.User.LoginResponse;
import com.example.eticaretjava.model.User.RegisterRequest;
import com.example.eticaretjava.model.User.RegisterResponse;
import com.example.eticaretjava.model.User.UserDto;

public interface AuthApi {

    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest body);

    @POST("auth/register")
    Call<ApiResponse<RegisterResponse>> register(@Body RegisterRequest body);

    @GET("me")
    Call<ApiResponse<UserDto>> me(@Header("Authorization") String auth);
}

