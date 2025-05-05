package com.example.perisaiapps.Model

import com.google.firebase.Timestamp

data class Mentor(
    val name: String = "",
    val peminatan: String = "",
    val photoUrl: String = "",
    val availableUntil: Timestamp? = null
)
