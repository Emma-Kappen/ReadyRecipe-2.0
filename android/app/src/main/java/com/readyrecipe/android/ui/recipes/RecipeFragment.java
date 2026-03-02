package com.readyrecipe.android.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.readyrecipe.android.R;
import com.readyrecipe.android.adapters.RecipeSuggestionAdapter;
import com.readyrecipe.android.data.MockRecipeRepository;
import com.readyrecipe.android.domain.GenerateRecipesUseCase;
import com.readyrecipe.android.domain.model.GeneratedRecipe;
import com.readyrecipe.android.models.Recipe;
import com.readyrecipe.android.network.ApiClient;
import com.readyrecipe.android.network.ApiService;
import com.readyrecipe.android.network.SessionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeFragment extends Fragment {
    private RecipeSuggestionAdapter adapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private TextView subtitle;
    private GenerateRecipesUseCase generateRecipesUseCase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recipeRecycler);
        View refreshButton = view.findViewById(R.id.btnRefreshRecipes);
        subtitle = view.findViewById(R.id.recipeSubtitle);
        View topBarTitle = view.findViewById(R.id.topBarTitle);
        if (topBarTitle instanceof TextView) {
            ((TextView) topBarTitle).setText("Recipes");
        }

        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        generateRecipesUseCase = new GenerateRecipesUseCase(new MockRecipeRepository());

        adapter = new RecipeSuggestionAdapter(this::cookRecipe);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        refreshButton.setOnClickListener(v -> loadRecipes());
        loadRecipes();
    }

    private void loadRecipes() {
        String timeOfDay = computeTimeOfDay();
        subtitle.setText(String.format(Locale.getDefault(), "Time of day: %s", timeOfDay));
        Call<List<Recipe>> call = apiService.suggestRecipes(timeOfDay);
        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setRecipes(response.body());
                } else if (response.code() == 404) {
                    Toast.makeText(requireContext(), "Endpoint pending", Toast.LENGTH_SHORT).show();
                    loadGeneratedRecipesFallback();
                } else {
                    Toast.makeText(requireContext(), "Failed to load recipes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadGeneratedRecipesFallback();
            }
        });
    }

    private void loadGeneratedRecipesFallback() {
        RecipeGenerationInterop.generate(getViewLifecycleOwner(), generateRecipesUseCase, new ArrayList<>(), (generatedRecipes, error) -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                return;
            }
            adapter.setRecipes(mapGeneratedRecipes(generatedRecipes));
        });
    }

    private List<Recipe> mapGeneratedRecipes(List<GeneratedRecipe> generatedRecipes) {
        List<Recipe> mapped = new ArrayList<>();
        for (GeneratedRecipe generatedRecipe : generatedRecipes) {
            Recipe recipe = new Recipe();
            recipe.setId(UUID.randomUUID());
            recipe.setName(generatedRecipe.getName());
            recipe.setCuisineType("ai-generated");
            recipe.setCookingTime(generatedRecipe.getEstimatedTime());
            recipe.setRating((double) generatedRecipe.getConfidenceScore());
            mapped.add(recipe);
        }
        return mapped;
    }

    private void cookRecipe(Recipe recipe) {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Login required", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<Void> call = apiService.cookRecipe(recipe.getId().toString(), userId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Cooked " + recipe.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to cook", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String computeTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 11) {
            return "breakfast";
        } else if (hour < 15) {
            return "lunch";
        } else if (hour < 21) {
            return "dinner";
        }
        return "snack";
    }

    private List<Recipe> buildMockRecipes(String timeOfDay) {
        List<Recipe> mock = new ArrayList<>();

        Recipe first = new Recipe();
        first.setId(UUID.randomUUID());
        first.setName("Quick " + capitalize(timeOfDay) + " Bowl");
        first.setCuisineType("home");
        first.setCookingTime(15);
        first.setRating(4.4);

        Recipe second = new Recipe();
        second.setId(UUID.randomUUID());
        second.setName("Pantry Veggie Wrap");
        second.setCuisineType("fusion");
        second.setCookingTime(20);
        second.setRating(4.2);

        mock.add(first);
        mock.add(second);
        return mock;
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "Recipe";
        }
        return value.substring(0, 1).toUpperCase(Locale.getDefault()) + value.substring(1);
    }
}
