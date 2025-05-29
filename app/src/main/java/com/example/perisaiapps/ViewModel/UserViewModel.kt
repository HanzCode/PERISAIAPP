package com.example.perisaiapps.viewmodel // Sesuaikan package Anda

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

// Enum untuk merepresentasikan peran agar lebih aman dari typo
enum class UserRole {
    ADMIN, MENTOR, USER, UNKNOWN
}

class UserViewModel : ViewModel() {

    private val _userRole = mutableStateOf<UserRole>(UserRole.UNKNOWN)
    val userRole: State<UserRole> = _userRole

    private val _isLoadingRole = mutableStateOf(false)
    val isLoadingRole: State<Boolean> = _isLoadingRole

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // Fungsi ini dipanggil setelah user berhasil login
    fun fetchUserRole() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            Log.w("UserViewModel", "No authenticated user found. Clearing role.")
            _userRole.value = UserRole.UNKNOWN
            _errorMessage.value = "Pengguna tidak terautentikasi."
            return
        }

        val uid = firebaseUser.uid
        _isLoadingRole.value = true
        _errorMessage.value = null // Reset error

        FirebaseFirestore.getInstance().collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val roleString = document.getString("role")
                    _userRole.value = when (roleString?.lowercase()) { // Konversi ke lowercase untuk konsistensi
                        "admin" -> UserRole.ADMIN
                        "mentor" -> UserRole.MENTOR
                        "user" -> UserRole.USER
                        else -> {
                            Log.w("UserViewModel", "Unknown role string: $roleString")
                            UserRole.UNKNOWN
                        }
                    }
                    Log.d("UserViewModel", "Role fetched for UID $uid: ${_userRole.value}")
                } else {
                    Log.d("UserViewModel", "No such user document for UID $uid, role is UNKNOWN.")
                    _userRole.value = UserRole.UNKNOWN // Atau peran default jika dokumen tidak ditemukan
                    _errorMessage.value = "Dokumen profil pengguna tidak ditemukan."
                }
                _isLoadingRole.value = false
            }
            .addOnFailureListener { exception ->
                Log.e("UserViewModel", "Error getting user role for UID $uid: ", exception)
                _userRole.value = UserRole.UNKNOWN
                _errorMessage.value = "Gagal mengambil peran pengguna: ${exception.message}"
                _isLoadingRole.value = false
            }
    }

    fun clearUserSession() {
        _userRole.value = UserRole.UNKNOWN
        _isLoadingRole.value = false
        _errorMessage.value = null
        // Anda mungkin juga ingin melakukan FirebaseAuth.getInstance().signOut() di sini atau di tempat lain yang sesuai
    }
}