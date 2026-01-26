package com.example.veganism

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class AddRecipeNextActivity : AppCompatActivity() {

    val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val chat = model.startChat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_recipe_next)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d("GeminiKey", BuildConfig.GEMINI_API_KEY)

        val intent = getIntent()
        val recipeName = intent.getStringExtra("recipeName")
        val recipeDescription = intent.getStringExtra("recipeDescription")
        val recipeImage = intent.getStringExtra("recipeImage")

        val etRecipeInfo = findViewById<EditText>(R.id.addRecipeNext_recipeInfo_et)
        val etCookingTime = findViewById<EditText>(R.id.addRecipeNext_cookingTime_et)

        val submitBtn = findViewById<Button>(R.id.addRecipeNext_submit_btn)
        submitBtn.setOnClickListener {
            showLoadingOverlay()
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val store = FirebaseFirestore.getInstance()

            store.collection("users").document(user!!.uid).get()
                .addOnSuccessListener {
                    val chefUsername = it.getString("username").toString()

                    lifecycleScope.launch {

                        val geminiRecipe = generateGeminiResponse(
                            etRecipeInfo.text.toString(),
                            etCookingTime.text.toString()
                        )

                        val recipe = Recipe(
                            "",
                            recipeName.toString(),
                            recipeDescription.toString(),
                            chefUsername,
                            "",
                            geminiRecipe.ingredients,
                            geminiRecipe.instructions,
                            geminiRecipe.notes,
                            geminiRecipe.cookingTime
                        )

                        val imageUri = recipeImage!!.toUri()

                        store.collection("recipes").add(recipe)
                            .addOnSuccessListener { document ->
                                val recipeId = document.id
                                document.update("id", recipeId)
                                val storage = FirebaseStorage.getInstance()
                                storage.getReference("recipes_images/$recipeId.jpg")
                                    .putFile(imageUri)
                                    .addOnSuccessListener {
                                        document.update("recipeImage", "$recipeId.jpg")
                                            .addOnSuccessListener {
                                                hideLoadingOverlay()
                                                Toast.makeText(this@AddRecipeNextActivity, "Recipe added successfully", Toast.LENGTH_SHORT).show()

                                                val intent = Intent(this@AddRecipeNextActivity, RecipeDetailsActivity::class.java)
                                                intent.putExtra("recipeId", recipeId)
                                                intent.putExtra("fromAddRecipe", true)
                                                startActivity(intent)
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

                                                finish()
                                            }
                                            .addOnFailureListener {
                                                hideLoadingOverlay()
                                                Toast.makeText(this@AddRecipeNextActivity, "Error adding recipe's image", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                            }
                            .addOnFailureListener {
                                hideLoadingOverlay()
                                Toast.makeText(this@AddRecipeNextActivity, "Error adding recipe", Toast.LENGTH_SHORT).show()
                            }

                    }
                }
                .addOnFailureListener {
                    hideLoadingOverlay()
                    Toast.makeText(this, "Error adding recipe", Toast.LENGTH_LONG).show()
                }
        }
    }

    private suspend fun generateGeminiResponse(
        userRecipeInput: String,
        userTimeInput: String
    ): RecipeParts {
        return try {
            val systemPromptRecipe = """
                You will be provided with vegan recipe content written as unstructured text. This text may include ingredients, preparation steps, and additional relevant details.
                Your task is to transform this content into a well-structured recipe suitable for display in a recipe application. The output must be friendly, clear, and professional, written in a natural tone similar to recipes found on popular cooking websites.
                
                Output Structure:
                Ingredients – Present a clean, organized list of all ingredients.
                Instructions – Present a numbered, step-by-step sequence of preparation instructions.
                Notes (optional) – Include only if relevant.
                
                Instruction Guidelines:
                Combine closely related actions into a single step (for example, adding an ingredient and cooking it should appear in the same instruction).
                When two verbs appear together, separate them clearly (for example, write “Add the mushrooms and cook for 5 minutes,” not “Add and cook the mushrooms for 5 minutes”).
                Refer to ingredients using “the” once they have been introduced (for example, “Add the onions,” not “Add onions”).
                Ensure the instructions are friendly, concise, professional, and easy to follow.
                Remove any non-recipe content, including promotions, advertisements, personal commentary, branding, or external references.

                Notes Guidelines:                    
                The Notes section may include helpful opinions, variations, tips, or serving suggestions.
                Do not include unnecessary or trivial statements such as “The dish is ready!” or “You have created a pasta.”
                Only include notes that add real value to understanding, preparing, or serving the recipe.
                
                Only include content directly related to the recipe itself.
                
                Formatting Requirement
                You must separate each section using the following exact markers on their own lines:
                [[INGREDIENTS]]
                [[INSTRUCTIONS]]
                [[NOTES]] (only if notes exist)

                Do not write anything outside these sections.
                """.trimIndent()
            val systemPromptTime = """
                You will be given a single string representing a duration of time. The input may contain numbers, symbols, or words, and may be written in various formats.
                    
                Examples of possible inputs:
                1:20
                01:20
                1h 20m
                1 hour and 20 minutes
                80 minutes
                about 1 hour
                1hr
                half an hour
                45 min
                2 hours
                1h20
                90
                
                Your task:
                Convert the provided time expression into a single integer representing the total number of minutes.
                
                Rules:
                - Interpret H:MM and HH:MM formats as hours and minutes (e.g., 1:20 = 80 minutes).
                - If only a number is provided without units, assume it represents minutes.
                - Ignore words such as “about” or “approximately”.
                - Convert common phrases (e.g., “half an hour”, “quarter hour”) appropriately.
                - Combine hours and minutes into total minutes.
                - Round to the nearest whole minute if necessary.
                
                Output requirements:
                - Output only a single integer.
                - Do not include any text, explanation, formatting, punctuation, extra spaces, or newlines.
                
                Examples:
                1:20 -> 80
                1 hour 20 minutes -> 80
                about 45 min -> 45
                half an hour -> 30
                2 hours -> 120
                90 -> 90
                """.trimIndent()


            val recipeSectionsResponse =
                model.generateContent("$systemPromptRecipe\n\nUser input:\n$userRecipeInput").text.toString()
            val cookingTimeResponse =
                model.generateContent("$systemPromptTime\n\nUser input:\n$userTimeInput").text.toString()


            Log.d("GeminiResponseRecipe", recipeSectionsResponse)
            Log.d("GeminiResponseTime", cookingTimeResponse)

            parseRecipe(recipeSectionsResponse, cookingTimeResponse)
        } catch (e: Exception) {
            Log.e("GeminiError", e.message ?: "Unknown error")
            RecipeParts("", "", "", 0)
        }
    }

    data class RecipeParts(
        val ingredients: String,
        val instructions: String,
        val notes: String,
        val cookingTime: Int
    )

    fun parseRecipe(recipeSectionsResponse: String, cookingTimeResponse: String): RecipeParts {
        if (recipeSectionsResponse == "No response from Gemini.") {
            return RecipeParts("", "", "", 0)
        }

        val ingredients = recipeSectionsResponse
            .substringAfter("[[INGREDIENTS]]")
            .substringBefore("[[INSTRUCTIONS]]")
            .trim()

        val instructions = recipeSectionsResponse
            .substringAfter("[[INSTRUCTIONS]]")
            .substringBefore("[[NOTES]]")
            .trim()

        val notes = if (recipeSectionsResponse.contains("[[NOTES]]")) {
            recipeSectionsResponse.substringAfter("[[NOTES]]").trim()
        } else {
            ""
        }

        return RecipeParts(ingredients, instructions, notes, cookingTimeResponse.trim().toInt())
    }

    fun formatWithDots(text: String): String {
        return text
            .lines()
            .filter { it.isNotBlank() }
            .joinToString("\n") { "• $it" }
    }

    fun formatWithNumbers(text: String): String {
        return text
            .lines()
            .filter { it.isNotBlank() }
            .mapIndexed { index, line ->
                "${index + 1}. $line"
            }
            .joinToString("\n")
    }

    fun showLoadingOverlay() {
        findViewById<FrameLayout>(R.id.addRecipeNext_loadingOverlay_fl).visibility = View.VISIBLE
    }

    fun hideLoadingOverlay() {
        findViewById<FrameLayout>(R.id.addRecipeNext_loadingOverlay_fl).visibility = View.GONE
    }
}