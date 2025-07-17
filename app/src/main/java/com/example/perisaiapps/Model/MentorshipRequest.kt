package com.example.perisaiapps.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Kita tetap gunakan nama MentorshipRequest agar konsisten
data class MentorshipRequest(
    @DocumentId
    val id: String = "", // Diisi otomatis oleh Firestore
    val menteeId: String = "",
    val mentorId: String = "",
    val menteeName: String = "",
    val menteePhotoUrl: String = "",
    val mentorName: String = "",
    val mentorPhotoUrl: String = "",
    val status: String = "NOT_SENT", // NOT_SENT, PENDING, ACCEPTED, DECLINED, COMPLETED
    val requestTimestamp: Timestamp = Timestamp.now(),
    val sessionCount: Long = 0
)