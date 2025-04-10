package com.example.perisaiapps.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(/* private val authRepository: AuthRepository */) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    // Instance FirebaseAuth (atau gunakan dari Repository)
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun loginUser(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _loginState.value = LoginUiState.Error("Email dan Password tidak boleh kosong")
            return
        }

        _loginState.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _loginState.value = LoginUiState.Success
                        } else {
                            _loginState.value = LoginUiState.Error(task.exception?.message ?: "Login Gagal")
                        }
                    }
            } catch (e: Exception) {
                _loginState.value = LoginUiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }
}

// Definisikan state UI
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}