package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Mentor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminManageMentorsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _mentors = MutableStateFlow<List<Mentor>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // State Flow yang sudah difilter untuk ditampilkan di UI
    val filteredMentors = combine(_mentors, _searchQuery) { mentors, query ->
        if (query.isBlank()) {
            mentors
        } else {
            mentors.filter {
                it.name.contains(query, ignoreCase = true) || it.peminatan.contains(query, ignoreCase = true)
            }
        }
    }

    init {
        listenForMentors()
    }

    private fun listenForMentors() {
        _isLoading.value = true
        db.collection("Mentor")
            .orderBy("name")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("AdminMentorsVM", "Listen failed.", e)
                    _errorMessage.value = "Gagal mengambil data: ${e.message}"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _mentors.value = snapshot.toObjects(Mentor::class.java)
                    _isLoading.value = false
                }
            }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteMentor(mentor: Mentor, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val mentorDocRef = db.collection("Mentor").document(mentor.id)
                val userDocRef = db.collection("users").document(mentor.userId)

                db.runBatch { batch ->
                    batch.delete(mentorDocRef)
                    batch.delete(userDocRef)
                }.await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Terjadi kesalahan tidak diketahui")
            }
        }
    }
}