package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Sealed class untuk merepresentasikan hasil login dengan lebih jelas
sealed class LoginResult {
    data object Idle : LoginResult()
    data object Loading : LoginResult()
    data class Success(val role: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val email = mutableStateOf("")
    val password = mutableStateOf("")

    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginResult = _loginResult.asStateFlow()

    // --- FUNGSI BARU UNTUK MENYIMPAN FCM TOKEN ---
    private fun saveFcmToken() {
        val userId = auth.currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("LoginVM_FCM", "Gagal mengambil FCM token", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("LoginVM_FCM", "FCM Token didapatkan: $token untuk UID: $userId")

            // Simpan token ke dokumen user di koleksi 'users'
            val userDocRef = db.collection("users").document(userId)
            userDocRef.update("fcmToken", token)
                .addOnSuccessListener { Log.d("LoginVM_FCM", "FCM Token berhasil disimpan di koleksi 'users'.") }
                .addOnFailureListener { e -> Log.w("LoginVM_FCM", "Gagal menyimpan FCM Token ke 'users'.", e) }

            // Simpan juga ke koleksi 'Mentor' jika rolenya mentor
            userDocRef.get().addOnSuccessListener {
                if(it.getString("role") == "mentor") {
                    db.collection("Mentor").whereEqualTo("userId", userId).limit(1).get()
                        .addOnSuccessListener { mentorQuery ->
                            if (!mentorQuery.isEmpty) {
                                mentorQuery.documents[0].reference.update("fcmToken", token)
                                    .addOnSuccessListener { Log.d("LoginVM_FCM", "FCM Token berhasil disimpan di koleksi 'Mentor'.") }
                            }
                        }
                }
            }
        }
    }

    fun signIn() {
        if (email.value.isBlank() || password.value.isBlank()) {
            _loginResult.value = LoginResult.Error("Semua field harus diisi.")
            return
        }

        viewModelScope.launch {
            _loginResult.value = LoginResult.Loading
            try {
                val authResult = auth.signInWithEmailAndPassword(email.value.trim(), password.value).await()
                val user = authResult.user

                if (user != null) {
                    // PANGGIL FUNGSI PENYIMPANAN TOKEN SETELAH LOGIN BERHASIL
                    saveFcmToken()

                    val userDoc = db.collection("users").document(user.uid).get().await()
                    val userRole = userDoc.getString("role") ?: "user"
                    _loginResult.value = LoginResult.Success(userRole)
                } else {
                    throw Exception("User tidak ditemukan setelah login.")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "Email tidak terdaftar."
                    is FirebaseAuthInvalidCredentialsException -> "Password salah."
                    else -> e.message ?: "Login gagal, silakan coba lagi."
                }
                _loginResult.value = LoginResult.Error(errorMessage)
            }
        }
    }

    fun clearLoginResult() {
        _loginResult.value = LoginResult.Idle
    }
}