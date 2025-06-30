package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.MentorChatListItem
import com.example.perisaiapps.Model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MentorChatListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _chatList = MutableStateFlow<List<MentorChatListItem>>(emptyList())
    val chatList = _chatList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchMentorChats()
    }

    fun fetchMentorChats() {
        val currentMentorId = auth.currentUser?.uid
        if (currentMentorId == null) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val chatRoomsSnapshot = db.collection("chats")
                    .whereArrayContains("participants", currentMentorId)
                    .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                    .get().await()

                val chatListItems = mutableListOf<MentorChatListItem>()

                for (doc in chatRoomsSnapshot.documents) {
                    val participants = doc.get("participants") as? List<*>
                    val menteeId = participants?.find { it != currentMentorId } as? String

                    if (menteeId != null) {
                        val userQuery = db.collection("users")
                            .whereEqualTo("userId", menteeId)
                            .limit(1)
                            .get()
                            .await()

                        if (!userQuery.isEmpty) {
                            val userProfile = userQuery.documents[0].toObject(User::class.java)

                            // ========================================================
                            // PERBAIKAN NAMA PARAMETER ADA DI SINI
                            // ========================================================
                            chatListItems.add(
                                MentorChatListItem(
                                    chatRoomId = doc.id,
                                    menteeId = menteeId,
                                    menteeName = userProfile?.displayName ?: "User", // <-- Diperbaiki
                                    menteePhotoUrl = userProfile?.photoUrl ?: "",     // <-- Diperbaiki
                                    lastMessage = doc.getString("lastMessageText") ?: "",
                                    lastMessageTimestamp = doc.getTimestamp("lastMessageTimestamp") ?: Timestamp.now()
                                )
                            )
                        } else {
                            Log.w("MentorChatListVM", "Profil mentee dengan userId '$menteeId' TIDAK DITEMUKAN di koleksi 'users'.")
                        }
                    }
                }
                _chatList.value = chatListItems
            } catch (e: Exception) {
                Log.e("MentorChatListVM", "Gagal mengambil daftar chat mentor", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}