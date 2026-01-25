package com.example.veganism

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RecipeAdapter(
    private val list: List<Recipe>,
    private val onItemClick: (Recipe, View, ImageView) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.recipeItem_recipeName_tv)
        val desc = itemView.findViewById<TextView>(R.id.recipeItem_recipeDescription_tv)
        val chef = itemView.findViewById<TextView>(R.id.recipeItem_chefName_tv)
        val image = itemView.findViewById<ImageView>(R.id.recipeItem_recipeImage_iv)
        val bookmark = itemView.findViewById<ImageView>(R.id.recipeItem_bookmarkIcon_v)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = list[position]
        val userUID = auth.currentUser!!.uid

        holder.name.text = recipe.name
        holder.desc.text = recipe.description
        holder.chef.text = "Chef: ${recipe.chefUsername}"

        storage.getReference("recipes_images/${recipe.recipeImage}").downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(holder.itemView.context)
                    .load(uri)
                    .into(holder.image)
            }
            .addOnFailureListener {
                holder.image.setImageResource(R.drawable.img_recipe_item_example)
            }

        holder.itemView.setOnClickListener {
            onItemClick(recipe, holder.itemView, holder.image)
        }

        // Check if the recipe is already saved in Firestore
        val recipeDocument = db.collection("users").document(userUID).collection("savedRecipes")
            .document(recipe.id)

        recipeDocument.get()
            .addOnSuccessListener { document ->
                recipe.isSaved = document.exists()
                updateBookmarkIcon(holder, recipe.isSaved)
            }

        holder.bookmark.setOnClickListener {
            recipe.isSaved = !recipe.isSaved
            if (recipe.isSaved) {
                // Save recipe to Firestore
                val recipeData = mapOf(
                    "name" to recipe.name,
                    "recipeImage" to recipe.recipeImage
                )
                recipeDocument.set(recipeData)
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Saved.", Toast.LENGTH_SHORT).show()
                        updateBookmarkIcon(holder, true)
                    }
                    .addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "Failed to save.", Toast.LENGTH_SHORT).show()
                        recipe.isSaved = false
                        updateBookmarkIcon(holder, false)
                    }
            } else {
                // Remove recipe from Firestore
                recipeDocument.delete()
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Removed.", Toast.LENGTH_SHORT).show()
                        updateBookmarkIcon(holder, false)
                    }
                    .addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "Failed to remove", Toast.LENGTH_SHORT).show()
                        recipe.isSaved = true
                        updateBookmarkIcon(holder, true)
                    }
            }
        }
    }
    private fun updateBookmarkIcon(holder: ViewHolder, isSaved: Boolean) {
        if (isSaved) {
            holder.bookmark.setImageResource(R.drawable.ic_bookmark_saved)
        } else {
            holder.bookmark.setImageResource(R.drawable.ic_bookmark_unsaved)
        }
    }


    override fun getItemCount() = list.size
}
