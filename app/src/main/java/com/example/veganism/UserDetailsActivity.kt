package com.example.veganism

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etUsername: EditText
    private lateinit var tvEmail: TextView
    private lateinit var etBirthYear: EditText
    private lateinit var rgIsVegan: RadioGroup
    private lateinit var tvIsVegan: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lateinit var firstName: String
        lateinit var lastName: String
        lateinit var username: String
        lateinit var birthYear: String
        var isVegan: Boolean = false // Cannot use 'lateinit' so applying default value

        etFirstName = findViewById(R.id.userDetails_firstName_et)
        etLastName = findViewById(R.id.userDetails_lastName_et)
        etUsername = findViewById(R.id.userDetails_username_et)
        tvEmail = findViewById(R.id.userDetails_email_tv)
        etBirthYear = findViewById(R.id.userDetails_birthYear_et)
        rgIsVegan = findViewById(R.id.userDetails_isVegan_rg)
        tvIsVegan = findViewById(R.id.userDetails_isVegan_tv)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        tvEmail.text = user!!.email
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener {
                val myUser = it.toObject(MyUser::class.java)
                firstName = myUser!!.firstName
                lastName = myUser.lastName
                username = myUser.username
                birthYear = myUser.birthYear.toString()
                isVegan = myUser.isVegan


                etFirstName.setText(firstName)
                etLastName.setText(lastName)
                etUsername.setText(username)
                etBirthYear.setText(birthYear)

                if(isVegan)
                    rgIsVegan.check(R.id.userDetails_yes_rb)
                else
                    rgIsVegan.check(R.id.userDetails_no_rb)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error getting user data.", Toast.LENGTH_LONG).show()
            }


        etFirstName.addTextChangedListener {
            val text = it.toString()
            etFirstName.isActivated = text != firstName
        }
        etLastName.addTextChangedListener {
            val text = it.toString()
            etLastName.isActivated = text != lastName
        }
        etUsername.addTextChangedListener {
            val text = it.toString()
            etUsername.isActivated = text != username
        }
        etBirthYear.addTextChangedListener {
            val text = it.toString()
            etBirthYear.isActivated = text != birthYear
        }
        rgIsVegan.setOnCheckedChangeListener { _, _ ->
            val changed = (rgIsVegan.checkedRadioButtonId == R.id.userDetails_yes_rb) != isVegan

            val rbIsVeganYes = findViewById<RadioButton>(R.id.userDetails_yes_rb)
            val rbIsVeganNo = findViewById<RadioButton>(R.id.userDetails_no_rb)

            tvIsVegan.isActivated = changed
            rbIsVeganYes.isActivated = changed
            rbIsVeganNo.isActivated = changed
        }


        val backBtn = findViewById<TextView>(R.id.userDetails_backArrow_iv)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        val btnReset = findViewById<Button>(R.id.userDetails_reset_btn)
        btnReset.setOnClickListener { resetFields() }

        val btnChange = findViewById<Button>(R.id.userDetails_change_btn)
        btnChange.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            val myUser = MyUser(
                etFirstName.text.toString(),
                etLastName.text.toString(),
                etUsername.text.toString(),
                etBirthYear.text.toString().toInt(),
                rgIsVegan.checkedRadioButtonId == R.id.userDetails_yes_rb,
                "${user!!.uid}.jpg"
            )

            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid).set(myUser)
                .addOnSuccessListener {
                    saveUserDetailsInPrefs()
                    Toast.makeText(this, "User Updated Successfully.", Toast.LENGTH_SHORT).show()

                    finish()
                    startActivity(Intent(this, MenuActivity::class.java))
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error Updating User.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (madeChanges()) {
            AlertDialog.Builder(this)
                .setTitle("Discard Changes?")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Yes") { _, _ -> super.onBackPressed() }
                .setNegativeButton("No", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun madeChanges(): Boolean {
        return etFirstName.isActivated || etLastName.isActivated || etUsername.isActivated || etBirthYear.isActivated || tvIsVegan.isActivated
    }
    private fun saveUserDetailsInPrefs() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit {
            putString("firstName", etFirstName.text.toString())
            putString("lastName", etLastName.text.toString())
            putString("username", etUsername.text.toString())
            apply()
        }
    }
    private fun resetFields() {
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user!!.uid).get()
            .addOnSuccessListener {
                etFirstName.setText(it.get("firstName").toString())
                etLastName.setText(it.get("lastName").toString())
                etUsername.setText(it.get("username").toString())
                etBirthYear.setText(it.get("birthYear").toString())
                if(it.get("isVegan").toString() == "true")
                {
                    rgIsVegan.check(R.id.userDetails_yes_rb)
                }
                else
                {
                    rgIsVegan.check(R.id.userDetails_no_rb)
                }
            }
    }
}