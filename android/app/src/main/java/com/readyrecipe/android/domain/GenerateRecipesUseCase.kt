package com.readyrecipe.android.domain

import com.readyrecipe.android.data.RecipeRepository
import com.readyrecipe.android.domain.model.GeneratedRecipe

class GenerateRecipesUseCase(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(pantry: List<String>): List<GeneratedRecipe> {
        return recipeRepository.generateRecipes(pantry)
    }
}
