package com.readyrecipe.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.readyrecipe.android.adapters.PantryAdapter;
import com.readyrecipe.android.models.PantryItem;
import com.readyrecipe.android.network.ApiClient;
import com.readyrecipe.android.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class PantryActivity extends AppCompatActivity {
    private PantryAdapter pantryAdapter;
    private RecyclerView recyclerViewPantry;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        sharedPreferences = getSharedPreferences("ReadyRecipePrefs", MODE_PRIVATE);
        
        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);
        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));
        pantryAdapter = new PantryAdapter();
        recyclerViewPantry.setAdapter(pantryAdapter);

        String userId = sharedPreferences.getString("userId", "");
        if (userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadPantryItems(userId);
    }

    private void loadPantryItems(String userId) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<PantryItem>> call = apiService.getPantryItems(userId);
        call.enqueue(new Callback<List<PantryItem>>() {
            @Override
            public void onResponse(Call<List<PantryItem>> call, Response<List<PantryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pantryAdapter.setItems(response.body());
                } else {
                    Toast.makeText(PantryActivity.this, "Failed to load pantry items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PantryItem>> call, Throwable t) {
                Toast.makeText(PantryActivity.this, "Error loading pantry: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
