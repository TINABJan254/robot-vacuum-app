package com.ht.robotvacuumapp.api.service;

import com.ht.robotvacuumapp.api.dto.LoginRequest;
import com.ht.robotvacuumapp.api.dto.LoginResponse;
import com.ht.robotvacuumapp.api.dto.RegisterRequest;
import com.ht.robotvacuumapp.api.dto.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("api/v1/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
