package com.readyrecipe.android.domain.model

data class GeneratedRecipe(
    val name: String,
    val estimatedTime: Int,
    val missingIngredients: List<String>,
    val confidenceScore: Float
)
