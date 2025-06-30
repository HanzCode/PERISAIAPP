package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Mentor
import com.example.perisaiapps.Model.UserChatListItem
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserChatListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _chatList = MutableStateFlow<List<UserChatListItem>>(emptyList())
    val chatList = _chatList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchUserChats()
    }

    private fun fetchUserChats() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val chatRoomsSnapshot = db.collection("chats")
                    .whereArrayContains("participants", currentUserId)
                    .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                    .get().await()

                val chatListItems = mutableListOf<UserChatListItem>()

                for (doc in chatRoomsSnapshot.documents) {
                    val participants = doc.get("participants") as? List<*>
                    val mentorId = participants?.find { it != currentUserId } as? String

                    if (mentorId != null) {
                        val mentorQuery = db.collection("Mentor")
                            .whereEqualTo("userId", mentorId)
                            .limit(1)
                            .get()
                            .await()

                        // Cek apakah hasil kueri tidak kosong
                        if (!mentorQuery.isEmpty) {
                            val mentorDoc = mentorQuery.documents[0] // Ambil dokumen pertama
                            val mentorProfile = mentorDoc.toObject(Mentor::class.java)

                            chatListItems.add(
                                UserChatListItem(
                                    chatRoomId = doc.id,
                                    mentorId = mentorId,
                                    mentorName = mentorProfile?.name ?: "Mentor Tanpa Nama",
                                    mentorPhotoUrl = mentorProfile?.photoUrl ?: "",
                                    lastMessage = doc.getString("lastMessageText") ?: "",
                                    lastMessageTimestamp = doc.getTimestamp("lastMessageTimestamp") ?: Timestamp.now()
                                )
                            )
                        }
                    }
                }
                _chatList.value = chatListItems
            } catch (e: Exception) {
                Log.e("UserChatListVM", "Gagal mengambil daftar chat", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}