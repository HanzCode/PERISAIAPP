package com.example.perisaiapps.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class ChatMessage(
    @DocumentId
    val id: String = "",
    val text: String = "", // Akan kosong jika pesannya gambar
    val imageUrl: String? = null, // URL gambar dari Cloudinary
    val type: String = "TEXT",
    val senderId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val fileUrl: String? = null,
    val fileName: String? = null,
    @get:Exclude val status: String = "UPLOADING", // "UPLOADING", "SENT", "FAILED"
    @get:Exclude val localUri: String? = null // Untuk pratinjau gambar/file sebelum diunggah
)
