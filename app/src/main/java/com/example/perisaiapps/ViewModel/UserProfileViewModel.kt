package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchCurrentUserProfile()
    }

    fun fetchCurrentUserProfile() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Ambil data dari koleksi 'users' menggunakan UID sebagai ID dokumen
                val doc = db.collection("users").document(currentUserId).get().await()
                if (doc != null && doc.exists()) {
                    _userProfile.value = doc.toObject(User::class.java)
                } else {
                    Log.w("UserProfileVM", "Dokumen user tidak ditemukan untuk UID: $currentUserId")
                    _userProfile.value = null
                }
            } catch (e: Exception) {
                Log.e("UserProfileVM", "Gagal mengambil profil user", e)
                _userProfile.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    // Fungsi ini agar bisa dipanggil untuk refresh setelah edit profil
    fun refreshProfile() {
        fetchCurrentUserProfile()
    }
}