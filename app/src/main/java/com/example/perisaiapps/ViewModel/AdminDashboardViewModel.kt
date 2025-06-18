package com.example.perisaiapps.ViewModel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.AggregateSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminDashboardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // State untuk jumlah mentor
    private val _mentorCount = MutableStateFlow(0L) // Gunakan Long untuk hasil count
    val mentorCount = _mentorCount.asStateFlow()

    // State untuk jumlah lomba
    private val _lombaCount = MutableStateFlow(0L)
    val lombaCount = _lombaCount.asStateFlow()

    // State untuk loading dan error
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Panggil fungsi fetch saat ViewModel pertama kali dibuat
    init {
        fetchDashboardStats()
    }

    fun fetchDashboardStats() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Mengambil jumlah dokumen di koleksi "Mentor"
                val mentorQuery = db.collection("Mentor").count()
                val mentorSnapshot = mentorQuery.get(AggregateSource.SERVER).await()
                _mentorCount.value = mentorSnapshot.count
                Log.d("AdminVM", "Total Mentor: ${mentorSnapshot.count}")

                // Mengambil jumlah dokumen di koleksi "Lomba"
                val lombaQuery = db.collection("Lomba").count()
                val lombaSnapshot = lombaQuery.get(AggregateSource.SERVER).await()
                _lombaCount.value = lombaSnapshot.count
                Log.d("AdminVM", "Total Lomba: ${lombaSnapshot.count}")

            } catch (e: Exception) {
                Log.e("AdminVM", "Error fetching dashboard stats", e)
                _errorMessage.value = "Gagal mengambil data statistik."
            } finally {
                _isLoading.value = false
            }
        }
    }
}