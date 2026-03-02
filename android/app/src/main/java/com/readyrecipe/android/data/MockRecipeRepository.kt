package com.readyrecipe.android.data

import com.readyrecipe.android.domain.model.GeneratedRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MockRecipeRepository : RecipeRepository {
    override suspend fun generateRecipes(pantry: List<String>): List<GeneratedRecipe> = withContext(Dispatchers.IO) {
        listOf(
            GeneratedRecipe(
                name = "Creamy Tomato Pasta",
                estimatedTime = 25,
                missingIngredients = listOf("parmesan", "garlic"),
                confidenceScore = 0.94f
            ),
            GeneratedRecipe(
                name = "Chicken Stir Fry",
                estimatedTime = 20,
                missingIngredients = listOf("soy sauce", "bell pepper"),
                confidenceScore = 0.89f
            ),
            GeneratedRecipe(
                name = "Vegetable Omelette",
                estimatedTime = 12,
                missingIngredients = listOf("eggs", "spinach"),
                confidenceScore = 0.87f
            )
        )
    }
}
