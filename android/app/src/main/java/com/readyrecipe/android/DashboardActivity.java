package com.readyrecipe.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.readyrecipe.android.models.DashboardStatsDTO;
import com.readyrecipe.android.network.ApiClient;
import com.readyrecipe.android.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sharedPreferences = getSharedPreferences("ReadyRecipePrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        if (userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadDashboardStats(userId);
    }

    private void loadDashboardStats(String userId) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<DashboardStatsDTO> call = apiService.getPantryStats(userId);
        call.enqueue(new Callback<DashboardStatsDTO>() {
            @Override
            public void onResponse(Call<DashboardStatsDTO> call, Response<DashboardStatsDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardStatsDTO stats = response.body();
                    updateStatsUI(stats);
                } else {
                    Toast.makeText(DashboardActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DashboardStatsDTO> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Error loading stats: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatsUI(DashboardStatsDTO stats) {
        // Find and update stat cards
        // The layout uses 4 include statements for item_stat_card
        // Each stat_card has tvStatValue and tvStatLabel
        // We need to find them dynamically; for now, use a simple approach
        try {
            TextView tvValue0 = findStatValueByIndex(0);
            if (tvValue0 != null) {
                tvValue0.setText(String.valueOf(stats.getTotalItems()));
            }
            TextView tvLabel0 = findStatLabelByIndex(0);
            if (tvLabel0 != null) {
                tvLabel0.setText("Total Items");
            }

            TextView tvValue1 = findStatValueByIndex(1);
            if (tvValue1 != null) {
                tvValue1.setText(String.valueOf(stats.getExpiringSoonCount()));
            }
            TextView tvLabel1 = findStatLabelByIndex(1);
            if (tvLabel1 != null) {
                tvLabel1.setText("Expiring Soon");
            }

            TextView tvValue2 = findStatValueByIndex(2);
            if (tvValue2 != null) {
                tvValue2.setText(String.valueOf(stats.getRecipesSaved()));
            }
            TextView tvLabel2 = findStatLabelByIndex(2);
            if (tvLabel2 != null) {
                tvLabel2.setText("Recipes Saved");
            }

            TextView tvValue3 = findStatValueByIndex(3);
            if (tvValue3 != null) {
                tvValue3.setText(String.format("%.1f%%", stats.getFoodSavingsPercent()));
            }
            TextView tvLabel3 = findStatLabelByIndex(3);
            if (tvLabel3 != null) {
                tvLabel3.setText("Food Savings");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Helper to find stat cards by index (may need adjustment if layout changes)
    private TextView findStatValueByIndex(int index) {
        int[] ids = {R.id.statsRow}; // statsRow is the parent container
        try {
            android.view.ViewGroup statsRow = findViewById(R.id.statsRow);
            if (statsRow != null && index < statsRow.getChildCount()) {
                android.view.View card = statsRow.getChildAt(index);
                if (card != null) {
                    return card.findViewById(R.id.tvStatValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private TextView findStatLabelByIndex(int index) {
        try {
            android.view.ViewGroup statsRow = findViewById(R.id.statsRow);
            if (statsRow != null && index < statsRow.getChildCount()) {
                android.view.View card = statsRow.getChildAt(index);
                if (card != null) {
                    return card.findViewById(R.id.tvStatLabel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
