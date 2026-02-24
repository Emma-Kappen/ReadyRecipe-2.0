package com.readyrecipe.android.network;

import com.readyrecipe.android.models.DashboardStatsDTO;
import com.readyrecipe.android.models.LoginRequest;
import com.readyrecipe.android.models.LoginResponse;
import com.readyrecipe.android.models.PantryItem;
import com.readyrecipe.android.models.Recipe;
import com.readyrecipe.android.models.SignUpRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import java.util.List;

public interface ApiService {
    @POST("/api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/api/signup")
    Call<LoginResponse> signUp(@Body SignUpRequest request);

    @GET("/api/recipes")
    Call<List<Recipe>> getRecipes();

    @GET("/api/recipes")
    Call<List<Recipe>> getRecipesByCuisine(@Query("cuisine") String cuisine);

    @GET("/api/pantry")
    Call<List<PantryItem>> getPantryItems(@Query("userId") String userId);

    @GET("/api/pantry/stats")
    Call<DashboardStatsDTO> getPantryStats(@Query("userId") String userId);
}
