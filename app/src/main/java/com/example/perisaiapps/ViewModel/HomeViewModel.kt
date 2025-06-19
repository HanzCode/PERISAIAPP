package com.example.perisaiapps.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Lomba
import com.example.perisaiapps.Model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // State untuk menampung data profil pengguna
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    // State untuk menampung daftar lomba
    private val _lombaList = MutableStateFlow<List<Lomba>>(emptyList())
    val lombaList = _lombaList.asStateFlow()

    // State untuk loading dan error
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        fetchHomeScreenData()
    }

    private fun fetchHomeScreenData() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _errorMessage.value = "Pengguna tidak terautentikasi."
                _isLoading.value = false
                return@launch
            }

            try {
                // Jalankan pengambilan data user dan lomba secara bersamaan (paralel)
                val userProfileDeferred = async { db.collection("users").document(userId).get().await() }
                val lombaListDeferred = async {
                    db.collection("Lomba")
                        .orderBy("namaLomba") // Urutkan berdasarkan nama atau field lain
                        .limit(5)
                        .get()
                        .await()
                }

                // Tunggu kedua proses selesai
                val userProfileDoc = userProfileDeferred.await()
                val lombaSnapshot = lombaListDeferred.await()

                // Proses hasil profil pengguna
                _userProfile.value = userProfileDoc.toObject(UserProfile::class.java)

                // Proses hasil daftar lomba
                _lombaList.value = lombaSnapshot.documents.mapNotNull { it.toObject(Lomba::class.java)?.copy(id = it.id) }

                Log.d("HomeViewModel", "Data fetched successfully. User: ${_userProfile.value?.displayName}, Lomba: ${_lombaList.value.size}")

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching home screen data", e)
                _errorMessage.value = "Gagal memuat data."
            } finally {
                _isLoading.value = false
            }
        }
    }
}