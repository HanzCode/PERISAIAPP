package com.example.perisaiapps.Model

import com.google.firebase.firestore.DocumentId

data class Mentor(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val peminatan: String = "",
    val deskripsi: String = "",
    val photoUrl: String = "",
    val bersediaKah : Boolean = true,
    val achievements: List<String>? = null,
    val fcmToken: String = ""
)
