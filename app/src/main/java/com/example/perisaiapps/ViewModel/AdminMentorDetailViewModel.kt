package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Mentor
import com.example.perisaiapps.Model.MentorshipRequest
import com.example.perisaiapps.Model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class helper untuk menampilkan hasil gabungan
data class MentorshipHistory(val mentee: User, val sessionCount: Long)

class AdminMentorDetailViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _mentor = MutableStateFlow<Mentor?>(null)
    val mentor = _mentor.asStateFlow()

    // State untuk mentee yang sedang aktif dibimbing
    private val _ongoingMentees = MutableStateFlow<List<User>>(emptyList())
    val ongoingMentees = _ongoingMentees.asStateFlow()

    // State untuk riwayat mentee yang sudah selesai
    private val _completedHistory = MutableStateFlow<List<MentorshipHistory>>(emptyList())
    val completedHistory = _completedHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadMentorData(mentorId: String) {
        if (mentorId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _ongoingMentees.value = emptyList()
            _completedHistory.value = emptyList()
            try {
                val mentorDoc = db.collection("Mentor").document(mentorId).get().await()
                val mentorData = mentorDoc.toObject(Mentor::class.java)
                _mentor.value = mentorData

                if (mentorData != null) {
                    val requestsSnapshot = db.collection("mentorship_requests")
                        .whereEqualTo("mentorId", mentorData.userId)
                        .get().await()

                    val allRequests = requestsSnapshot.toObjects(MentorshipRequest::class.java)
                    val allMenteeIds = allRequests.map { it.menteeId }.distinct()

                    if (allMenteeIds.isNotEmpty()) {
                        val usersSnapshot = db.collection("users").whereIn("userId", allMenteeIds).get().await()
                        val allMenteeProfiles = usersSnapshot.toObjects(User::class.java).associateBy { it.userId }

                        // Isi daftar yang sedang berjalan (ACCEPTED)
                        _ongoingMentees.value = allRequests
                            .filter { it.status == "ACCEPTED" }
                            .mapNotNull { allMenteeProfiles[it.menteeId] }

                        // Isi daftar riwayat yang sudah selesai (COMPLETED)
                        _completedHistory.value = allRequests
                            .filter { it.status == "COMPLETED" }
                            .mapNotNull { request ->
                                allMenteeProfiles[request.menteeId]?.let { mentee ->
                                    // sessionCount diambil langsung dari dokumen, tidak perlu dihitung
                                    MentorshipHistory(mentee, request.sessionCount)
                                }
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminMentorDetailVM", "Gagal mengambil data riwayat", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}