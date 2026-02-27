package com.readyrecipe.android;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.readyrecipe.android.adapters.RecipeAdapter;
import com.readyrecipe.android.models.Recipe;
import com.readyrecipe.android.network.ApiClient;
import com.readyrecipe.android.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class RecipesActivity extends AppCompatActivity {
    private RecipeAdapter recipeAdapter;
    private RecyclerView recyclerRecipes;
    private Button btnAll, btnItalian, btnAsian, btnHealth;
    private String currentFilter = null; // null = all

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        recyclerRecipes = findViewById(R.id.recyclerRecipes);
        recyclerRecipes.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter();
        recyclerRecipes.setAdapter(recipeAdapter);

        btnAll = findViewById(R.id.btnAll);
        btnItalian = findViewById(R.id.btnItalian);
        btnAsian = findViewById(R.id.btnAsian);
        btnHealth = findViewById(R.id.btnHealth);

        btnAll.setOnClickListener(v -> {
            currentFilter = null;
            loadRecipes(null);
        });
        btnItalian.setOnClickListener(v -> {
            currentFilter = "Italian";
            loadRecipes("Italian");
        });
        btnAsian.setOnClickListener(v -> {
            currentFilter = "Chinese";
            loadRecipes("Chinese");
        });
        btnHealth.setOnClickListener(v -> {
            currentFilter = "Indian";
            loadRecipes("Indian");
        });

        loadRecipes(null);
    }

    private void loadRecipes(String cuisine) {
        ApiService apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);
        Call<List<Recipe>> call = (cuisine != null) 
            ? apiService.getRecipesByCuisine(cuisine)
            : apiService.getRecipes();
        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recipeAdapter.setRecipes(response.body());
                } else {
                    Toast.makeText(RecipesActivity.this, "Failed to load recipes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Toast.makeText(RecipesActivity.this, "Error loading recipes: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
