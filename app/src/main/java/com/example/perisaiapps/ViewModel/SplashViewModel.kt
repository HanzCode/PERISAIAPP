package com.example.perisaiapps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _nextRoute = MutableStateFlow<String?>(null)
    val nextRoute = _nextRoute.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                // Jika tidak ada user, langsung ke halaman login
                _nextRoute.value = "login"
            } else {
                // Jika ada user, cek rolenya di Firestore
                try {
                    val userDoc = db.collection("users").document(currentUser.uid).get().await()
                    val role = userDoc.getString("role")
                    _nextRoute.value = when (role) {
                        "admin" -> "admin_dashboard_route"
                        "mentor" -> "mentor_main_route"
                        else -> "home"
                    }
                } catch (e: Exception) {
                    // Jika gagal ambil role, arahkan ke login untuk keamanan
                    _nextRoute.value = "login"
                }
            }
        }
    }
}