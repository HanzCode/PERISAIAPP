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
import com.google.firebase.firestore.SetOptions
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

    // State ini sekarang akan menampung seluruh objek hubungan, atau null jika tidak ada
    private val _mentorshipRequest = MutableStateFlow<MentorshipRequest?>(null)
    val mentorshipRequest = _mentorshipRequest.asStateFlow()

    // Fungsi tunggal untuk memuat semua data yang diperlukan
    fun loadData(mentorDocumentId: String) {
        if (mentorDocumentId.isBlank()) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Ambil profil mentor berdasarkan ID dokumennya
                val mentorDoc = db.collection("Mentor").document(mentorDocumentId).get().await()
                val mentorData = mentorDoc.toObject(Mentor::class.java)
                _mentor.value = mentorData

                // 2. Jika mentor ditemukan, gunakan userId-nya untuk memulai listener ke dokumen permintaan
                if (mentorData != null) {
                    listenForRequestStatus(mentorData.userId)
                }
            } catch (e: Exception) {
                Log.e("DetailMentorVM", "Gagal memuat data awal", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun listenForRequestStatus(mentorUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        // Buat ID dokumen yang konsisten
        val requestId = if (currentUserId < mentorUserId) "${currentUserId}_${mentorUserId}" else "${mentorUserId}_${currentUserId}"

        db.collection("mentorship_requests").document(requestId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("DetailMentorVM", "Listen failed.", error)
                    _mentorshipRequest.value = null
                    return@addSnapshotListener
                }
                // Simpan seluruh objeknya, atau null jika tidak ada
                _mentorshipRequest.value = snapshot?.toObject(MentorshipRequest::class.java)
            }
    }

    fun sendMentorshipRequest(mentor: Mentor) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                val userProfile = userDoc.toObject(User::class.java)
                val requestId = if (currentUser.uid < mentor.userId) "${currentUser.uid}_${mentor.userId}" else "${mentor.userId}_${currentUser.uid}"

                val requestData = MentorshipRequest(
                    // id tidak perlu diisi manual
                    menteeId = currentUser.uid,
                    mentorId = mentor.userId,
                    menteeName = userProfile?.displayName ?: "User",
                    menteePhotoUrl = userProfile?.photoUrl ?: "",
                    mentorName = mentor.name,
                    mentorPhotoUrl = mentor.photoUrl,
                    status = "PENDING",
                    requestTimestamp = Timestamp.now()
                )

                db.collection("mentorship_requests").document(requestId).set(requestData, SetOptions.merge())
            } catch (e: Exception) {
                Log.e("DetailMentorVM", "Gagal mengirim permintaan: ${e.message}")
            }
        }
    }
}