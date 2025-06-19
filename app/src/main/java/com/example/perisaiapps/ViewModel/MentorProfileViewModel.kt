package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Mentor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MentorProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _mentorProfile = MutableStateFlow<Mentor?>(null)
    val mentorProfile = _mentorProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchCurrentMentorProfile()
    }

    private fun fetchCurrentMentorProfile() {
        val userId = auth.currentUser?.uid
        Log.d("MentorProfileVM", "Mencoba fetch profil. User ID: $userId")

        if (userId == null) {
            _isLoading.value = false
            Log.w("MentorProfileVM", "Gagal: auth.currentUser.uid adalah null.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val querySnapshot = db.collection("Mentor")
                    .whereEqualTo("userId", userId)
                    .limit(1)
                    .get()
                    .await()
                Log.d("MentorProfileVM", "Query ke Firestore berhasil.")

                if (!querySnapshot.isEmpty) {
                    _mentorProfile.value = querySnapshot.documents[0].toObject(Mentor::class.java)
                    Log.d("MentorProfileVM", "Sukses: Menemukan dokumen untuk mentor: ${_mentorProfile.value?.name}")
                } else {
                    Log.w("MentorProfileVM", "Gagal: Query berhasil tapi tidak ada dokumen yang cocok untuk userId: $userId")
                    _mentorProfile.value = null
                }
            } catch (e: Exception) {
                Log.e("MentorProfileVM", "Error saat query ke Firestore", e)
                _mentorProfile.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun logout() {
        auth.signOut()
    }
}