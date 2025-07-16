package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Mentor
import com.example.perisaiapps.Model.MentorshipRequest
import com.example.perisaiapps.Model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DetailMentorViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _mentor = MutableStateFlow<Mentor?>(null)
    val mentor = _mentor.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _requestStatus = MutableStateFlow("LOADING") // "LOADING", "NOT_SENT", "PENDING", "ACCEPTED", "DECLINED"
    val requestStatus = _requestStatus.asStateFlow()

    fun fetchMentorDetail(mentorId: String) {
        if (mentorId.isBlank()) {
            _errorMessage.value = "ID Mentor tidak valid."
            _isLoading.value = false
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // KEMBALIKAN KE CARA LAMA: Ambil dokumen langsung berdasarkan ID-nya
                val doc = db.collection("Mentor").document(mentorId).get().await()

                if (doc.exists()) {
                    _mentor.value = doc.toObject(Mentor::class.java)
                } else {
                    _errorMessage.value = "Data mentor tidak ditemukan."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil detail: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkRequestStatus(mentorUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val requestId = "${currentUserId}_${mentorUserId}"
        db.collection("mentorship_requests").document(requestId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status")
                    // Jika statusnya COMPLETED, anggap seperti belum pernah request
                    if (status == "COMPLETED") {
                        _requestStatus.value = "NOT_SENT"
                    } else {
                        _requestStatus.value = status ?: "ERROR"
                    }
                } else {
                    _requestStatus.value = "NOT_SENT" // Belum ada permintaan
                }
            }
    }

    fun sendMentorshipRequest(mentor: Mentor) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                val userProfile = userDoc.toObject(User::class.java)

                val requestId = "${currentUser.uid}_${mentor.userId}"
                val newRequest = MentorshipRequest(
                    id = requestId,
                    menteeId = currentUser.uid,
                    mentorId = mentor.userId,
                    menteeName = userProfile?.displayName ?: "User Baru",
                    menteePhotoUrl = userProfile?.photoUrl ?: "",
                    status = "PENDING",
                    requestTimestamp = Timestamp.now()
                )
                db.collection("mentorship_requests").document(requestId).set(newRequest).await()
            } catch (e: Exception) {
                Log.e("DetailMentorVM", "Gagal mengirim permintaan: ${e.message}")
            }
        }
    }

    fun refreshData(mentorId: String) {
        fetchMentorDetail(mentorId)
    }
}