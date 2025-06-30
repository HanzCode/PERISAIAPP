package com.example.perisaiapps.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class SharedNote(
    @DocumentId
    val id: String = "",
    val text: String = "",
    val editorId: String = "",
    val lastEdited: Timestamp = Timestamp.now()
)