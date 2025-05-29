package com.example.perisaiapps.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Mentor(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val peminatan: String = "",
    val Deskripsi: String = "",
    val photoUrl: String = "",
    val availableUntil: Timestamp? = null
)
