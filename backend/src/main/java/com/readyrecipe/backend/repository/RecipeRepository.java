package com.readyrecipe.backend.repository;

import com.readyrecipe.backend.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    List<Recipe> findByCuisineType(String cuisineType);
}
