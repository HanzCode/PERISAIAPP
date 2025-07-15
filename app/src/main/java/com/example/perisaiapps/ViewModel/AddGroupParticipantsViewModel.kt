package com.example.perisaiapps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddGroupParticipantsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    // Fungsi untuk mengambil semua user, KECUALI yang sudah ada di chat
    fun loadUsers(existingParticipants: List<String>) {
        viewModelScope.launch {
            try {

                val result = db.collection("users")
                    .whereEqualTo("role", "user")
                    .get().await()

                val allUsersWithUserRole = result.toObjects(User::class.java)

                _users.value = allUsersWithUserRole.filter { it.userId !in existingParticipants }

            } catch (e: Exception) {
            }
        }
    }
}