package com.readyrecipe.app.api;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/refresh")
    Call<LoginResponse> refreshToken(@Query("token") String refreshToken);

    @GET("api/pantry")
    Call<List<PantryItem>> getPantry();

    @POST("api/pantry/add")
    Call<PantryItem> addToPantry(@Body PantryItem item);

    @DELETE("api/pantry/{id}")
    Call<ResponseBody> deletePantryItem(@Path("id") String id);
}
