package com.readyrecipe.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.readyrecipe.backend.entity.Recipe;
import com.readyrecipe.backend.repository.RecipeRepository;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    @GetMapping("")
    public ResponseEntity<List<Recipe>> listRecipes(@RequestParam(name = "cuisine", required = false) String cuisine) {
        if (cuisine != null && !cuisine.isEmpty()) {
            List<Recipe> r = recipeRepository.findByCuisineType(cuisine);
            return ResponseEntity.ok(r);
        }
        List<Recipe> all = recipeRepository.findAll();
        return ResponseEntity.ok(all);
    }
}
