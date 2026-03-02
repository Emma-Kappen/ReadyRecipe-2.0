package com.readyrecipe.android.ui.recipes

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.readyrecipe.android.domain.GenerateRecipesUseCase
import com.readyrecipe.android.domain.model.GeneratedRecipe
import kotlinx.coroutines.launch

fun interface GeneratedRecipeCallback {
    fun onResult(recipes: List<GeneratedRecipe>, error: String?)
}

object RecipeGenerationInterop {
    @JvmStatic
    fun generate(
        owner: LifecycleOwner,
        useCase: GenerateRecipesUseCase,
        pantry: List<String>,
        callback: GeneratedRecipeCallback
    ) {
        owner.lifecycleScope.launch {
            runCatching {
                useCase(pantry)
            }.onSuccess { generatedRecipes ->
                callback.onResult(generatedRecipes, null)
            }.onFailure { throwable ->
                callback.onResult(emptyList(), throwable.message ?: "Failed to generate recipes")
            }
        }
    }
}
