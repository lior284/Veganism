package com.example.veganism

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RecipeDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isFromAddRecipe = intent.getBooleanExtra("fromAddRecipe", false)

        if (!isFromAddRecipe) {
            supportPostponeEnterTransition() // Only postpone if the user came from the list of recipes and not from the add recipe
        }

        setContentView(R.layout.activity_recipe_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val back = findViewById<TextView>(R.id.back)
        back.setOnClickListener {
            if (isFromAddRecipe) {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                prefs.edit().putString("lastFragment", "HomeFragment").apply()

                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            } else {
                supportFinishAfterTransition()
            }
        }

        val recipeId = intent.getStringExtra("recipeId").toString()

        val recipeImage = findViewById<ImageView>(R.id.recipeDetails_recipeImage_iv)
        val recipeName = findViewById<TextView>(R.id.recipeDetails_recipeName_tv)
        val recipeDescription = findViewById<TextView>(R.id.recipeDetails_recipeDescription_tv)
        val recipeIngredients = findViewById<TextView>(R.id.recipeDetails_recipeIngredients_tv)
        val recipeInstructions = findViewById<TextView>(R.id.recipeDetails_recipeInstructions_tv)
        val recipeNotesTitle = findViewById<TextView>(R.id.recipeDetails_recipeNotesTitle_tv)
        val recipeNotes = findViewById<TextView>(R.id.recipeDetails_recipeNotes_tv)
        val recipeChef = findViewById<TextView>(R.id.recipeDetails_recipeChef_tv)


        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        db.collection("recipes").document(recipeId).get()
            .addOnSuccessListener {
                storage.getReference("recipes_images/${it.getString("recipeImage")}").downloadUrl
                    .addOnSuccessListener { uri ->
                        Glide.with(this)
                            .load(uri)
                            .dontAnimate()
                            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                                override fun onLoadFailed(
                                    e: com.bumptech.glide.load.engine.GlideException?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    supportStartPostponedEnterTransition()
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: android.graphics.drawable.Drawable,
                                    model: Any,
                                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                                    dataSource: com.bumptech.glide.load.DataSource,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    if (!isFromAddRecipe) {
                                        supportStartPostponedEnterTransition()
                                    }

                                    val recipeFadeIn = findViewById<LinearLayout>(R.id.recipeDetails_fadeIn_ll)

                                    // 1. Position the text slightly lower than its final spot
                                    recipeFadeIn.translationY = 100f
                                    recipeFadeIn.alpha = 0f

                                    // 2. Animate it slowly to its original position (translationY = 0)
                                    recipeFadeIn.animate()
                                        .translationY(0f) // Move up to its natural spot
                                        .alpha(1f) // Fade in
                                        .setDuration(500)
                                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                                        .start()

                                    return false
                                }
                            })
                            .into(recipeImage)
                    }
                    .addOnFailureListener {
                        recipeImage.setImageResource(R.drawable.img_recipe_item_example)
                    }

                recipeName.text = it.getString("name")
                recipeDescription.text = it.getString("description")
                recipeIngredients.text = it.get("ingredients").toString()
                recipeInstructions.text = it.get("instructions").toString()

                val notes = it.get("notes").toString()
                if (notes == "") {
                    recipeNotesTitle.visibility = View.GONE
                    recipeNotes.visibility = View.GONE
                } else {
                    recipeNotesTitle.visibility = View.VISIBLE
                    recipeNotes.visibility = View.VISIBLE
                    recipeNotes.text = it.get("notes").toString()
                }

                recipeChef.text = "Recipe by ${it.getString("chefUsername")}"
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
}