package com.readyrecipe.android.data

import com.readyrecipe.android.domain.model.GeneratedRecipe

interface RecipeRepository {
    suspend fun generateRecipes(pantry: List<String>): List<GeneratedRecipe>
}
