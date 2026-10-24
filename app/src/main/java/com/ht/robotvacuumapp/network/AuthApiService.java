package com.ht.robotvacuumapp.network;

import com.ht.robotvacuumapp.network.model.LoginRequest;
import com.ht.robotvacuumapp.network.model.LoginResponse;
import com.ht.robotvacuumapp.network.model.RegisterRequest;
import com.ht.robotvacuumapp.network.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("api/v1/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
