package com.readyrecipe.android.network;

import com.readyrecipe.android.models.LoginRequest;
import com.readyrecipe.android.models.LoginResponse;
import com.readyrecipe.android.models.SignUpRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/api/signup")
    Call<LoginResponse> signUp(@Body SignUpRequest request);
}
