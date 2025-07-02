package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.ChatMessage
import com.example.perisaiapps.Model.SharedNote
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val messageText = mutableStateOf("")
    private val _notes = MutableStateFlow<List<SharedNote>>(emptyList())
    val notes = _notes.asStateFlow()

    // --- FUNGSI sendMessage DIPERBAIKI TOTAL ---
    fun sendMessage(chatRoomId: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null || messageText.value.isBlank() || chatRoomId.isBlank()) return

        val participants = chatRoomId.split("_")
        val otherUserId = participants.firstOrNull { it != currentUserId } ?: return

        val textToSend = messageText.value
        val now = Timestamp.now()

        // Reset field input di UI segera agar terasa responsif
        messageText.value = ""

        viewModelScope.launch {
            val chatRoomRef = db.collection("chats").document(chatRoomId)
            try {
                // --- LOGIKA PENULISAN BARU YANG LEBIH AMAN ---

                // Langkah 1: Buat/Update dokumen chat utama untuk memastikan field 'participants' ada
                // Ini penting agar kueri 'whereArrayContains' di list chat tidak gagal
                val initialChatData = mapOf("participants" to listOf(currentUserId, otherUserId))
                chatRoomRef.set(initialChatData, SetOptions.merge()).await()

                // Langkah 2: Tambahkan dokumen pesan baru ke sub-koleksi
                val messageData = mapOf(
                    "text" to textToSend,
                    "senderId" to currentUserId,
                    "timestamp" to now,
                    "isRead" to false
                )
                chatRoomRef.collection("messages").add(messageData).await()

                // Langkah 3: Lakukan UPDATE terpisah untuk menaikkan hitungan dan info pesan terakhir
                // .update() akan membuat field jika belum ada dan bisa menjalankan increment
                val updateData = mapOf(
                    "lastActivityTimestamp" to now,
                    "lastMessageText" to textToSend,
                    "unreadCounts.${otherUserId}" to FieldValue.increment(1)
                )
                chatRoomRef.update(updateData).await()

                Log.d("ChatVM_Send", "Pesan terkirim dan unreadCount di-increment dengan sukses.")

            } catch (e: Exception) {
                Log.e("ChatVM_Send", "Gagal mengirim pesan atau update unreadCount", e)
                messageText.value = textToSend // Kembalikan teks jika gagal
            }
        }
    }

    // --- Sisa fungsi lain sudah benar dan tidak perlu diubah ---
    fun getMessages(chatRoomId: String) = callbackFlow {
        if (chatRoomId.isBlank()) { close(); return@callbackFlow }
        val messagesCollection = db.collection("chats").document(chatRoomId).collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
        val listener = messagesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) { trySend(snapshot.toObjects(ChatMessage::class.java)).isSuccess }
        }
        awaitClose { listener.remove() }
    }

    fun markMessagesAsRead(chatRoomId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val chatRoomRef = db.collection("chats").document(chatRoomId)
                val updates = mapOf(
                    "unreadCounts.${currentUserId}" to 0,
                    "lastActivityTimestamp" to Timestamp.now()
                )
                chatRoomRef.update(updates).await()
                Log.d("ChatVM", "Unread count dan activity timestamp diupdate.")
            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal mereset unread count", e)
            }
        }
    }

    fun getNotes(chatRoomId: String) {
        if (chatRoomId.isBlank()) return
        val notesCollection = db.collection("chats").document(chatRoomId).collection("notes")
            .orderBy("lastEdited", Query.Direction.DESCENDING)
        notesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) { return@addSnapshotListener }
            if (snapshot != null) { _notes.value = snapshot.toObjects(SharedNote::class.java) }
        }
    }

    fun upsertNote(chatRoomId: String, noteText: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val noteRef = db.collection("chats").document(chatRoomId).collection("notes").document("shared_note")
            val noteData = mapOf("text" to noteText, "editorId" to currentUserId, "lastEdited" to Timestamp.now())
            try {
                noteRef.set(noteData, SetOptions.merge()).await()
            } catch (e: Exception) { Log.e("ChatVM", "Gagal menyimpan catatan", e) }
        }
    }
}