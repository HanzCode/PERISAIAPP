package com.example.perisaiapps.Model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val docId: String = "",
    val userId: String = "",       // Field untuk menyimpan UID dari Authentication
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val role: String = ""
)