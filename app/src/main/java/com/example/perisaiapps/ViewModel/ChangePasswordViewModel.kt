package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Sealed class untuk menampung hasil operasi
sealed class ChangePasswordResult {
    data object Idle : ChangePasswordResult()
    data object Loading : ChangePasswordResult()
    data class Success(val message: String) : ChangePasswordResult()
    data class Error(val message: String) : ChangePasswordResult()
}

class ChangePasswordViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // State untuk setiap field input
    val oldPassword = mutableStateOf("")
    val newPassword = mutableStateOf("")
    val confirmPassword = mutableStateOf("")

    // State untuk hasil operasi
    private val _changePasswordResult = MutableStateFlow<ChangePasswordResult>(ChangePasswordResult.Idle)
    val changePasswordResult = _changePasswordResult.asStateFlow()

    fun changePassword() {
        // 1. Validasi Input
        if (oldPassword.value.isBlank() || newPassword.value.isBlank() || confirmPassword.value.isBlank()) {
            _changePasswordResult.value = ChangePasswordResult.Error("Semua field harus diisi.")
            return
        }
        if (newPassword.value.length < 6) {
            _changePasswordResult.value = ChangePasswordResult.Error("Password baru minimal harus 6 karakter.")
            return
        }
        if (newPassword.value != confirmPassword.value) {
            _changePasswordResult.value = ChangePasswordResult.Error("Password baru dan konfirmasi tidak cocok.")
            return
        }

        val user = auth.currentUser
        if (user?.email == null) {
            _changePasswordResult.value = ChangePasswordResult.Error("Sesi pengguna tidak valid.")
            return
        }

        viewModelScope.launch {
            _changePasswordResult.value = ChangePasswordResult.Loading
            try {
                // 2. Re-autentikasi: Buktikan bahwa ini benar-benar Anda dengan memasukkan password lama
                val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword.value)
                user.reauthenticate(credential).await()

                Log.d("ChangePasswordVM", "Re-autentikasi berhasil.")

                // 3. Jika berhasil, baru update password ke yang baru
                user.updatePassword(newPassword.value).await()

                Log.d("ChangePasswordVM", "Update password berhasil.")
                _changePasswordResult.value = ChangePasswordResult.Success("Password berhasil diperbarui!")

            } catch (e: Exception) {
                Log.e("ChangePasswordVM", "Gagal mengubah password", e)
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Password lama yang Anda masukkan salah."
                    is FirebaseAuthWeakPasswordException -> "Password baru terlalu lemah."
                    else -> "Gagal mengubah password: ${e.message}"
                }
                _changePasswordResult.value = ChangePasswordResult.Error(errorMessage)
            }
        }
    }

    // Fungsi untuk mereset status setelah pesan ditampilkan
    fun clearResultStatus() {
        _changePasswordResult.value = ChangePasswordResult.Idle
    }
}