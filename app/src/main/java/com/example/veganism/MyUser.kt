package com.example.veganism

data class MyUser (
    var firstName: String = "",
    var lastName: String = "",
    var username: String = "",
    var birthYear: Int = 0,
    var isVegan: Boolean = false,
    var profilePicture: String = ""
)