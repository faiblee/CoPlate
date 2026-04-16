package com.faible.coplate.api;

import com.faible.coplate.model.AuthResponse;
import com.faible.coplate.model.LoginRequest;
import com.faible.coplate.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
}