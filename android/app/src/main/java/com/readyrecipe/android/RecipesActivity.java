package com.readyrecipe.android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class RecipesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);
        // TODO: populate RecyclerView with recipes from API
    }
}
