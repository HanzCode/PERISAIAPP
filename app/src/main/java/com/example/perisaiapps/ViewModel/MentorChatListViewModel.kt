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
        // Logika init sekarang hanya memanggil fungsi listener
        listenForMentorChats()
    }

    // FUNGSI INI KITA GANTI TOTAL UNTUK MENJADI REAL-TIME
    private fun listenForMentorChats() {
        val currentMentorId = auth.currentUser?.uid
        if (currentMentorId == null) {
            _isLoading.value = false
            return
        }
        _isLoading.value = true

        val query = db.collection("chats")
            .whereArrayContains("participants", currentMentorId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        // Gunakan .addSnapshotListener, BUKAN .get()
        query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("MentorChatListVM", "Error mendengarkan chat", error)
                _isLoading.value = false
                return@addSnapshotListener
            }

            if (snapshot != null) {

                viewModelScope.launch {
                    val chatListItems = mutableListOf<MentorChatListItem>()
                    for (doc in snapshot.documents) {
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
                                val unreadCountQuery = db.collection("chats").document(doc.id).collection("messages")
                                    .whereEqualTo("senderId", menteeId)
                                    .whereEqualTo("isRead", false)
                                    .get().await()
                                val unreadCount = unreadCountQuery.size()

                                chatListItems.add(
                                    MentorChatListItem(
                                        chatRoomId = doc.id,
                                        menteeId = menteeId,
                                        menteeName = userProfile?.displayName ?: "User",
                                        menteePhotoUrl = userProfile?.photoUrl ?: "",
                                        lastMessage = doc.getString("lastMessageText") ?: "",
                                        lastMessageTimestamp = doc.getTimestamp("lastMessageTimestamp") ?: Timestamp.now(),
                                        unreadCount = unreadCount
                                    )
                                )
                            }
                        }
                    }
                    _chatList.value = chatListItems
                    _isLoading.value = false
                }
            }
        }
    }
}