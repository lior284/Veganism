package com.example.veganism
import com.google.firebase.firestore.PropertyName

data class MyUser (
    var firstName: String = "",
    var lastName: String = "",
    var username: String = "",
    var birthYear: Int = 0,

    @get:PropertyName("isVegan")
    @set:PropertyName("isVegan")
    var isVegan: Boolean = false,
    var profilePicture: String = ""
)