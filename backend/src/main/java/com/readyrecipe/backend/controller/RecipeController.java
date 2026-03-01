package com.readyrecipe.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.readyrecipe.backend.entity.PantryItem;
import com.readyrecipe.backend.entity.Recipe;
import com.readyrecipe.backend.repository.PantryRepository;
import com.readyrecipe.backend.repository.RecipeRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PantryRepository pantryRepository;

    @GetMapping("")
    public ResponseEntity<List<Recipe>> listRecipes(@RequestParam(name = "cuisine", required = false) String cuisine) {
        if (cuisine != null && !cuisine.isEmpty()) {
            List<Recipe> r = recipeRepository.findByCuisineType(cuisine);
            return ResponseEntity.ok(r);
        }
        List<Recipe> all = recipeRepository.findAll();
        return ResponseEntity.ok(all);
    }

    @PostMapping("/suggest")
    public ResponseEntity<List<Recipe>> suggestRecipes(
            @RequestParam(name = "timeOfDay", required = false) String timeOfDay,
            @RequestParam(name = "userId", required = false) String userId) {
        List<Recipe> allRecipes = recipeRepository.findAll();
        List<PantryItem> pantryItems;
        try {
            if (userId != null && !userId.isBlank()) {
                pantryItems = pantryRepository.findByUserId(UUID.fromString(userId));
            } else {
                pantryItems = pantryRepository.findAll();
            }
        } catch (Exception e) {
            pantryItems = pantryRepository.findAll();
        }
        if (allRecipes.isEmpty()) {
            return ResponseEntity.ok(allRecipes);
        }

        Set<String> pantryKeywords = new HashSet<>();
        for (PantryItem pantryItem : pantryItems) {
            if (pantryItem.getItemName() != null && !pantryItem.getItemName().isBlank()) {
                pantryKeywords.add(pantryItem.getItemName().toLowerCase(Locale.getDefault()));
            }
        }

        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            String recipeName = recipe.getName() == null ? "" : recipe.getName().toLowerCase(Locale.getDefault());
            boolean pantryMatch = pantryKeywords.stream().anyMatch(recipeName::contains);
            if (pantryMatch) {
                if (matchesTimeOfDay(recipe, timeOfDay)) {
                    suggested.add(recipe);
                }
            }
        }

        if (!suggested.isEmpty()) {
            return ResponseEntity.ok(suggested);
        }

        return ResponseEntity.ok(allRecipes);
    }

    private boolean matchesTimeOfDay(Recipe recipe, String timeOfDay) {
        if (timeOfDay == null || timeOfDay.isBlank()) {
            return true;
        }
        int cookingTime = recipe.getCookingTime() == null ? 30 : recipe.getCookingTime();
        String slot = timeOfDay.toLowerCase(Locale.getDefault());
        if ("breakfast".equals(slot)) {
            return cookingTime <= 25;
        }
        if ("snack".equals(slot)) {
            return cookingTime <= 20;
        }
        return true;
    }
}
