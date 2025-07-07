package com.example.perisaiapps.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminManageUsersViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val functions: FirebaseFunctions = Firebase.functions("asia-southeast2") // Sesuaikan region Anda

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult = _operationResult.asStateFlow()

    init {
        fetchUsers()
    }

    fun fetchUsers() { /* ... Logika fetch users Anda yang sudah ada ... */ }

    fun resetPassword(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = hashMapOf("uid" to uid)
                val result = functions.getHttpsCallable("resetPasswordToDefault").call(data).await()
                val message = (result.data as? Map<*, *>)?.get("message") as? String
                _operationResult.value = message ?: "Operasi berhasil."
            } catch (e: Exception) {
                _operationResult.value = "Gagal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearOperationResult() {
        _operationResult.value = null
    }
}