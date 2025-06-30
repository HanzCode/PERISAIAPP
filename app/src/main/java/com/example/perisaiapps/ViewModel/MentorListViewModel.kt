package com.example.perisaiapps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Mentor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MentorListViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _mentors = MutableStateFlow<List<Mentor>>(emptyList())
    // State untuk query pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // State untuk daftar mentor yang sudah difilter
    val filteredMentors = _mentors
        .combine(_searchQuery) { mentors, query ->
            if (query.isBlank()) {
                mentors
            } else {
                mentors.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.peminatan.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Mulai berbagi saat ada subscriber
            initialValue = emptyList() // Nilai awal
        )

    init {
        fetchMentors()
    }

    // Fungsi ini tidak berubah
    fun fetchMentors() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = db.collection("Mentor").whereEqualTo("bersediaKah", true).get().await() // Ambil mentor yg bersedia saja
                _mentors.value = result.documents.mapNotNull { it.toObject(Mentor::class.java) }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi baru untuk memperbarui query pencarian
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}