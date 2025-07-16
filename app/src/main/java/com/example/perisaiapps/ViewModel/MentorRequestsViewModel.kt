package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.perisaiapps.Model.MentorshipRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MentorRequestsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _requests = MutableStateFlow<List<MentorshipRequest>>(emptyList())
    val requests = _requests.asStateFlow()

    init {
        listenForRequests()
    }

    private fun listenForRequests() {
        val mentorId = auth.currentUser?.uid ?: return
        db.collection("mentorship_requests")
            .whereEqualTo("mentorId", mentorId)
            .whereEqualTo("status", "PENDING")
            .orderBy("requestTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MentorRequestsVM", "Gagal listen: butuh indeks?", error)
                    return@addSnapshotListener
                }
                _requests.value = snapshot?.toObjects(MentorshipRequest::class.java) ?: emptyList()
            }
    }

    fun acceptRequest(request: MentorshipRequest) {
        val requestRef = db.collection("mentorship_requests").document(request.id)
        val chatRoomId = if (request.menteeId < request.mentorId) "${request.menteeId}_${request.mentorId}" else "${request.mentorId}_${request.menteeId}"
        val chatRoomRef = db.collection("chats").document(chatRoomId)

        db.runBatch { batch ->
            batch.update(requestRef, "status", "ACCEPTED")
            batch.set(
                chatRoomRef,
                mapOf(
                    "participants" to listOf(request.menteeId, request.mentorId),
                    "type" to "DIRECT",
                    "lastActivityTimestamp" to com.google.firebase.Timestamp.now(),
                    "lastMessageText" to "Bimbingan telah dimulai.",
                    "unreadCounts" to mapOf(request.menteeId to 0, request.mentorId to 0)
                ),
                SetOptions.merge()
            )
        }
    }

    fun declineRequest(requestId: String) {
        db.collection("mentorship_requests").document(requestId).update("status", "DECLINED")
    }
}