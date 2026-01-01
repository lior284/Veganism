package com.example.veganism

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddRecipeNextActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_recipe_next)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val intent = getIntent()
        val recipeName = intent.getStringExtra("recipeName")
        val recipeDescription = intent.getStringExtra("recipeDescription")
        val recipeImage = intent.getStringExtra("recipeImage")

        val hoursPicker = findViewById<NumberPicker>(R.id.addRecipeNext_hours_np)
        val minutesPicker = findViewById<NumberPicker>(R.id.addRecipeNext_minutes_np)

        hoursPicker.minValue = 0
        hoursPicker.maxValue = 23
        minutesPicker.minValue = 1
        minutesPicker.maxValue = 59

        hoursPicker.setOnValueChangedListener { _, _, newVal ->
            minutesPicker.minValue = if (newVal == 0) 1 else 0
        }


        styleNumberPicker(hoursPicker)
        styleNumberPicker(minutesPicker)
    }

    fun styleNumberPicker(picker: NumberPicker) {
        for (i in 0 until picker.childCount) {
            val child = picker.getChildAt(i)
            if (child is EditText) {
                child.setTextColor(Color.RED)
                child.typeface = ResourcesCompat.getFont(picker.context, R.font.quicksand_regular)
                child.textSize = 20f
            }
        }
    }

}