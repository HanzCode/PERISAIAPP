package com.example.perisaiapps.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class MentorshipRequest(
    @DocumentId
    val id: String = "",
    val menteeId: String = "",
    val mentorId: String = "",
    val menteeName: String = "",
    val menteePhotoUrl: String = "",
    val status: String = "PENDING",
    val requestTimestamp: Timestamp = Timestamp.now()
)