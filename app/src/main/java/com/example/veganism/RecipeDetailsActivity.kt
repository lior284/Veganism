package com.example.veganism

import android.os.Bundle
import android.view.View
import android.widget.ImageView
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
        setContentView(R.layout.activity_recipe_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val back = findViewById<TextView>(R.id.back)
        back.setOnClickListener {
            finish()
        }

        val recipeId = intent.getStringExtra("RECIPE_ID").toString()

        val recipeImage = findViewById<ImageView>(R.id.recipeDetails_recipeImage_iv)
        val recipeName = findViewById<TextView>(R.id.recipeDetails_recipeName_tv)
        val recipeDescription = findViewById<TextView>(R.id.recipeDetails_recipeDescription_tv)
        val recipeIngredients = findViewById<TextView>(R.id.recipeDetails_recipeIngredients_tv)
        val recipeInstructions = findViewById<TextView>(R.id.recipeDetails_recipeInstructions_tv)
        val recipeNotesTitle = findViewById<TextView>(R.id.recipeDetails_recipeNotesTitle_tv)
        val recipeNotes = findViewById<TextView>(R.id.recipeDetails_recipeNotes_tv)

        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        db.collection("recipes").document(recipeId).get()
            .addOnSuccessListener {
                storage.getReference("recipes_images/${it.getString("recipeImage")}").downloadUrl
                    .addOnSuccessListener { uri ->
                        Glide.with(this)
                            .load(uri)
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
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
}