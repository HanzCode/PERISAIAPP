package com.example.perisaiapps.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ChatRoom(
    @DocumentId
    val id: String = "",
    val participants: List<String> = emptyList(),
    val type: String = "DIRECT", // "DIRECT" atau "GROUP"
    val groupName: String? = null,
    val groupPhotoUrl: String? = null,
    val createdBy: String? = null, // UID pembuat grup
    val lastActivityTimestamp: Timestamp? = null,
    val lastMessageText: String? = ""
)