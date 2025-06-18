package com.example.perisaiapps.Model

import com.google.firebase.firestore.DocumentId

data class UserProfile(
    @DocumentId
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = ""
)