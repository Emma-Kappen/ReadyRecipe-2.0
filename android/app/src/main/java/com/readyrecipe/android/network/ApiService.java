package com.readyrecipe.android.network;

import com.readyrecipe.android.models.DashboardStatsDTO;
import com.readyrecipe.android.models.GroceryItem;
import com.readyrecipe.android.models.LoginRequest;
import com.readyrecipe.android.models.LoginResponse;
import com.readyrecipe.android.models.PantryItem;
import com.readyrecipe.android.models.Recipe;
import com.readyrecipe.android.models.SignUpRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @POST("/api/pantry")
    Call<PantryItem> addPantryItem(@Body PantryItem item);

    @POST("/api/pantry/bulk")
    Call<List<PantryItem>> addPantryItems(@Body List<PantryItem> items);

    @PUT("/api/pantry/{id}")
    Call<PantryItem> updatePantryItem(@Path("id") String id, @Body PantryItem item);

    @DELETE("/api/pantry/{id}")
    Call<Void> deletePantryItem(@Path("id") String id);

    @POST("/api/recipes/suggest")
    Call<List<Recipe>> suggestRecipes(@Query("timeOfDay") String timeOfDay);

    @POST("/api/recipes/{id}/cook")
    Call<Void> cookRecipe(@Path("id") String id, @Query("userId") String userId);

    @GET("/api/grocery")
    Call<List<GroceryItem>> getGroceryItems(@Query("userId") String userId);

    @POST("/api/grocery")
    Call<GroceryItem> addGroceryItem(@Body GroceryItem item);

    @PATCH("/api/grocery/{id}")
    Call<GroceryItem> updateGroceryItem(@Path("id") String id, @Body GroceryItem item);

    @POST("/api/grocery/generate")
    Call<List<GroceryItem>> generateGrocery(@Query("userId") String userId);
}
