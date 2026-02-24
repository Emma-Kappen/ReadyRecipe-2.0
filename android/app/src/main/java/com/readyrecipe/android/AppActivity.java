package com.readyrecipe.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AppActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        Button btnDashboard = findViewById(R.id.btnDashboard);
        Button btnPantry = findViewById(R.id.btnPantry);
        Button btnRecipes = findViewById(R.id.btnRecipes);

        btnDashboard.setOnClickListener(v -> startActivity(new Intent(AppActivity.this, DashboardActivity.class)));
        btnPantry.setOnClickListener(v -> startActivity(new Intent(AppActivity.this, PantryActivity.class)));
        btnRecipes.setOnClickListener(v -> startActivity(new Intent(AppActivity.this, RecipesActivity.class)));
    }
}
