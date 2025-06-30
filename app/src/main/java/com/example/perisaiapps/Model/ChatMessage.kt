package com.example.perisaiapps.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ChatMessage(
    @DocumentId
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)