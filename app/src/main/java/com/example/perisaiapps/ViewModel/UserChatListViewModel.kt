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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserChatListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _chatList = MutableStateFlow<List<UserChatListItem>>(emptyList())
    val chatList = _chatList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val totalUnreadCount = chatList
        .map { list -> list.sumOf { it.unreadCount } } // Hitung total dari list chat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        listenForUserChats()
    }

    private fun listenForUserChats() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _isLoading.value = false
            return
        }
        _isLoading.value = true
        val query = db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastActivityTimestamp", Query.Direction.DESCENDING)

        query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("UserChatListVM", "Error mendengarkan chat", error)
                _isLoading.value = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                viewModelScope.launch {
                    val chatListItems = mutableListOf<UserChatListItem>()
                    for (doc in snapshot.documents) {
                        val participants = doc.get("participants") as? List<*>
                        val mentorId = participants?.find { it != currentUserId } as? String

                        if (mentorId != null) {
                            val mentorQuery = db.collection("Mentor")
                                .whereEqualTo("userId", mentorId).limit(1).get().await()

                            if (!mentorQuery.isEmpty) {
                                val mentorProfile = mentorQuery.documents[0].toObject(Mentor::class.java)
                                Log.d("ChatListDebug", "--- Memproses Chat Room: ${doc.id} ---")

                                // Log 1: Lihat isi mentah dari field unreadCounts
                                val rawUnreadCounts = doc.get("unreadCounts")
                                Log.d("ChatListDebug", "Data mentah unreadCounts: $rawUnreadCounts (Tipe: ${rawUnreadCounts?.javaClass?.simpleName})")

                                // Log 2: Lihat hasil setelah di-cast ke Map<String, Long>
                                val unreadCounts = rawUnreadCounts as? Map<String, Long>
                                Log.d("ChatListDebug", "Data setelah cast ke Map: $unreadCounts")

                                // Log 3: Lihat UID yang kita gunakan untuk mencari
                                Log.d("ChatListDebug", "Mencari hitungan untuk currentUserId: $currentUserId")

                                // Log 4: Lihat nilai spesifik yang didapat dari map
                                val countValue = unreadCounts?.get(currentUserId)
                                Log.d("ChatListDebug", "Nilai yang didapat dari map: $countValue (Tipe: ${countValue?.javaClass?.simpleName})")

                                // Log 5: Lihat hasil akhir setelah konversi dan default
                                val unreadCount = countValue?.toInt() ?: 0
                                Log.d("ChatListDebug", "Hasil akhir unreadCount: $unreadCount")
                                Log.d("ChatListDebug", "--------------------------------------------------")
//                                unreadCount = unreadCounts?.get(currentUserId)?.toInt() ?: 0

                                chatListItems.add(
                                    UserChatListItem(
                                        chatRoomId = doc.id,
                                        mentorId = mentorId,
                                        mentorName = mentorProfile?.name ?: "Mentor",
                                        mentorPhotoUrl = mentorProfile?.photoUrl ?: "",
                                        lastMessage = doc.getString("lastMessageText") ?: "",
                                        lastActivityTimestamp = doc.getTimestamp("lastActivityTimestamp") ?: Timestamp.now(),
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