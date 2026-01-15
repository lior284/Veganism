package com.example.veganism

class Recipe (
    val id: String = "",
    var name: String = "",
    var description: String = "",
    var chefUsername: String = "",
    var recipeImage: String = "",
    var ingredients: String = "",
    var instructions: String = "",
    var notes: String = "",
    var cookingTimeMinutes: Int = 0, // The int represents the number of minutes
    var isSaved: Boolean = false
)