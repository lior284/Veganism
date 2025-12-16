package com.example.veganism

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class RecipeAdapter(
    private val list: List<Recipe>
) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

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

        holder.name.text = recipe.name
        holder.desc.text = recipe.description
        holder.chef.text = "Chef: " + recipe.chefUsername

        val storage = FirebaseStorage.getInstance()
        storage.getReference("recipes_images/" + recipe.recipeImage).downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(holder.itemView.context)
                    .load(uri)
                    .into(holder.image)
            }
            .addOnFailureListener {
                holder.image.setImageResource(R.drawable.img_recipe_item_example)
            }
        if(recipe.isSaved) {
            holder.bookmark.setImageResource(R.drawable.ic_bookmark_saved)
        } else {
            holder.bookmark.setImageResource(R.drawable.ic_bookmark_unsaved)
        }

        holder.bookmark.setOnClickListener {

            recipe.isSaved = !recipe.isSaved
            if(recipe.isSaved)
            {
                holder.bookmark.setImageResource(R.drawable.ic_bookmark_saved)
                Toast.makeText(holder.itemView.context, "Saved!", Toast.LENGTH_SHORT).show()
            } else {
                holder.bookmark.setImageResource(R.drawable.ic_bookmark_unsaved)
                Toast.makeText(holder.itemView.context, "Unsaved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = list.size
}
