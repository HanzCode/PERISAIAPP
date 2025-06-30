package com.example.perisaiapps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Mentor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DetailMentorViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _mentor = MutableStateFlow<Mentor?>(null)
    val mentor = _mentor.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

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
                val doc = db.collection("Mentor").document(mentorId).get().await()
                if (doc != null && doc.exists()) {
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
}