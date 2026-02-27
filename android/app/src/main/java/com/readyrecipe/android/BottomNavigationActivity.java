package com.readyrecipe.android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.readyrecipe.android.ui.camera.CameraFragment;
import com.readyrecipe.android.ui.grocery.GroceryFragment;
import com.readyrecipe.android.ui.pantry.PantryFragment;
import com.readyrecipe.android.ui.recipes.RecipeFragment;

public class BottomNavigationActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_camera) {
                fragment = new CameraFragment();
            } else if (itemId == R.id.nav_recipes) {
                fragment = new RecipeFragment();
            } else if (itemId == R.id.nav_grocery) {
                fragment = new GroceryFragment();
            } else {
                fragment = new PantryFragment();
            }
            replaceFragment(fragment);
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_pantry);
            replaceFragment(new PantryFragment());
        }
    }

    public void openCameraTab() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_camera);
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }
}
