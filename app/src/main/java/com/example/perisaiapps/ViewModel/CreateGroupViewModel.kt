package com.example.perisaiapps.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Mentor
import com.example.perisaiapps.Model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class GroupCreationState {
    data object Idle : GroupCreationState()
    data class Success(val newChatId: String) : GroupCreationState()
    data class Error(val message: String) : GroupCreationState()
}

class CreateGroupViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _mentors = MutableStateFlow<List<Mentor>>(emptyList())
    val mentors = _mentors.asStateFlow()

    // State untuk UI
    val groupName = mutableStateOf("")
    val selectedMentorId = mutableStateOf<String?>(null)
    val selectedUserIds = mutableStateOf<Set<String>>(emptySet())

    private val _creationState = MutableStateFlow<GroupCreationState>(GroupCreationState.Idle)
    val creationState = _creationState.asStateFlow()

    init {
        loadPotentialParticipants()
    }

    private fun loadPotentialParticipants() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Ambil semua mentor (tidak ada perubahan di sini)
                val mentorResult = db.collection("Mentor").get().await()
                _mentors.value = mentorResult.toObjects(Mentor::class.java)

                val userResult = db.collection("users")
                    .whereEqualTo("role", "user")
                    .get().await()
                val allUsersWithUserRole = userResult.toObjects(User::class.java)

                _users.value = allUsersWithUserRole.filter { it.userId != currentUserId }

            } catch (e: Exception) {
            }
        }
    }

    fun createGroup() {
        val currentUserId = auth.currentUser?.uid ?: return

        // Validasi
        if (groupName.value.isBlank()) {
            _creationState.value = GroupCreationState.Error("Nama grup tidak boleh kosong.")
            return
        }
        if (selectedMentorId.value == null) {
            _creationState.value = GroupCreationState.Error("Anda harus memilih satu mentor.")
            return
        }
        if (selectedUserIds.value.isEmpty()) {
            _creationState.value = GroupCreationState.Error("Anda harus memilih minimal satu teman.")
            return
        }

        viewModelScope.launch {
            val participants = mutableListOf(currentUserId, selectedMentorId.value!!)
            participants.addAll(selectedUserIds.value)

            val newChatRoomRef = db.collection("chats").document()
            val initialSystemMessage = "${auth.currentUser?.displayName ?: "Anda"} membuat grup."

            val chatRoomData = mapOf(
                "participants" to participants.distinct(),
                "type" to "GROUP",
                "groupName" to groupName.value,
                "createdBy" to currentUserId,
                "lastActivityTimestamp" to Timestamp.now(),
                "lastMessageText" to initialSystemMessage,
                "unreadCounts" to participants.distinct().associateWith { 0 }
            )
            val systemMessageData = mapOf(
                "text" to initialSystemMessage, "type" to "SYSTEM",
                "senderId" to currentUserId, "timestamp" to Timestamp.now()
            )

            try {
                db.runBatch { batch ->
                    batch.set(newChatRoomRef, chatRoomData)
                    batch.set(newChatRoomRef.collection("messages").document(), systemMessageData)
                }.await()
                _creationState.value = GroupCreationState.Success(newChatRoomRef.id)
            } catch (e: Exception) {
                _creationState.value = GroupCreationState.Error("Gagal membuat grup: ${e.message}")
            }
        }
    }

    fun clearCreationState() {
        _creationState.value = GroupCreationState.Idle
    }
}