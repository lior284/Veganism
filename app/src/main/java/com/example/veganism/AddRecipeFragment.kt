package com.example.veganism

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddRecipeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddRecipeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageUri: Uri
    private lateinit var ivRecipeImage: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_recipe, container, false)

        val etRecipeName = view.findViewById<EditText>(R.id.addRecipeFragment_recipeName_et)
        val etRecipeDescription = view.findViewById<EditText>(R.id.addRecipeFragment_recipeDescription_et)
        ivRecipeImage = view.findViewById<ImageView>(R.id.addRecipeFragment_recipeImage_iv)
        val btnTakePicture = view.findViewById<ImageView>(R.id.addRecipeFragment_takePicture_iv)
        val btnNext = view.findViewById<Button>(R.id.addRecipeFragment_next_btn)
        val btnReset = view.findViewById<Button>(R.id.addRecipeFragment_reset_btn)

        btnReset.setOnClickListener {
            etRecipeName.text.clear()
            etRecipeDescription.text.clear()
            ivRecipeImage.setImageResource(R.drawable.img_recipe_item_example)
        }

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val db = FirebaseFirestore.getInstance()

        var chefUsername = "Unknown"
        db.collection("users").document(user!!.uid).get()
            .addOnSuccessListener {
                chefUsername = it.getString("username").toString()
            }

        btnTakePicture.setOnClickListener {

            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
            } else {
                openCamera()
            }
        }

        // After the user pressed V
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    ivRecipeImage.setImageBitmap(it)
                    imageUri = saveBitmapToTempFile(it)
                }
            }
        }


        btnNext.setOnClickListener {

            val intent = Intent(requireContext(), AddRecipeNextActivity::class.java)

            val store = FirebaseFirestore.getInstance()

            val recipe = Recipe(
                etRecipeName.text.toString(),
                etRecipeDescription.text.toString(),
                chefUsername,
                ""
            )

            store.collection("recipes").add(recipe)
                .addOnSuccessListener { documentReference ->
                    val recipeId = documentReference.id
                    val storage = FirebaseStorage.getInstance()
                    storage.getReference("recipes_images/" + recipeId + ".jpg").putFile(imageUri)
                        .addOnSuccessListener {
                            documentReference.update("recipeImage", recipeId + ".jpg")
                                .addOnSuccessListener {
                                    Toast.makeText(this@AddRecipeFragment.context,"Recipe added successfully",Toast.LENGTH_SHORT).show()
                                }
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this@AddRecipeFragment.context, "Error adding recipe",Toast.LENGTH_SHORT).show()
                }

        }

        return view
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "temp_image.jpg")
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        }
        return Uri.fromFile(file)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(intent)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddRecipeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddRecipeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}