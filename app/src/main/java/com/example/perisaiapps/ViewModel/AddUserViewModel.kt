package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddUserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val displayName = mutableStateOf("")
    val role = mutableStateOf("user")

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _addUserStatus = MutableStateFlow<Pair<Boolean, String?>>(false to null)
    val addUserStatus = _addUserStatus.asStateFlow()

    fun createUser() {
        if (email.value.isBlank() || password.value.isBlank() || displayName.value.isBlank()) {
            _addUserStatus.value = false to "Semua field harus diisi."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _addUserStatus.value = false to null

            try {
                // Langkah 1: Buat user di Firebase Authentication
                val authResult = auth.createUserWithEmailAndPassword(email.value.trim(), password.value).await()
                val newUser = authResult.user

                if (newUser != null) {

                    val userProfile = UserProfile(
                        userId = newUser.uid, // <-- Mengisi field userId dengan UID dari Auth
                        displayName = displayName.value.trim(),
                        email = email.value.trim(),
                        role = role.value,
                        photoUrl = ""
                    )

                    // Simpan ke Firestore dengan ID dokumen sama dengan UID
                    db.collection("users").document(newUser.uid).set(userProfile).await()

                    Log.d("AddUserVM", "User berhasil dibuat di Auth dan Firestore dengan userId.")
                    _addUserStatus.value = true to "Pengguna baru berhasil ditambahkan!"
                } else {
                    throw Exception("Gagal mendapatkan data user setelah registrasi.")
                }

            } catch (e: Exception) {
                val errorMessage = e.message ?: "Terjadi kesalahan yang tidak diketahui."
                Log.e("AddUserVM", "Gagal membuat user", e)
                _addUserStatus.value = false to errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}