package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.ChatMessage
import com.example.perisaiapps.Model.SharedNote
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _notes = MutableStateFlow<List<SharedNote>>(emptyList())
    val notes = _notes.asStateFlow()

    val messageText = mutableStateOf("")

    fun getNotes(chatRoomId: String) {
        if (chatRoomId.isBlank()) return
        val notesCollection = db.collection("chats").document(chatRoomId).collection("notes")
            .orderBy("lastEdited", Query.Direction.DESCENDING)

        notesCollection.addSnapshotListener { snapshot, error ->
            if (snapshot != null) {
                _notes.value = snapshot.toObjects(SharedNote::class.java)
            }
        }
    }

    // Fungsi untuk menambah atau mengedit catatan
    fun upsertNote(chatRoomId: String, noteText: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (noteText.isBlank()) return // Jangan simpan catatan kosong

        viewModelScope.launch {
            val noteRef = db.collection("chats").document(chatRoomId).collection("notes")

            // Kita gunakan ID "shared_note" agar selalu mengedit dokumen yang sama.
            val sharedNoteDocRef = noteRef.document("shared_note")

            val noteData = mapOf(
                "text" to noteText,
                "editorId" to currentUserId,
                "lastEdited" to Timestamp.now()
            )
            // .set dengan merge=true akan membuat dokumen jika belum ada, atau update jika sudah ada
            sharedNoteDocRef.set(noteData, SetOptions.merge()).await()
        }
    }
    fun getMessages(chatRoomId: String) = callbackFlow {
        if (chatRoomId.isBlank()) { close(); return@callbackFlow }
        val messagesCollection = db.collection("chats").document(chatRoomId).collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
        val listener = messagesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ChatVM", "Gagal mendengarkan pesan", error)
                close(error); return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.toObjects(ChatMessage::class.java)).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    // --- FUNGSI INI KITA PERBAIKI LOGIKANYA ---
    fun markMessagesAsRead(chatRoomId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Dapatkan ID lawan bicara dari chatRoomId
        val participants = chatRoomId.split("_")
        val otherUserId = participants.firstOrNull { it != currentUserId }
        if (otherUserId == null) {
            Log.e("ChatVM", "Tidak bisa menandai pesan, otherUserId tidak ditemukan.")
            return
        }

        viewModelScope.launch {
            try {
                val messagesRef = db.collection("chats").document(chatRoomId).collection("messages")

                // Kueri yang lebih baik: cari pesan DARI lawan bicara, yang belum dibaca
                val unreadMessagesQuery = messagesRef
                    .whereEqualTo("senderId", otherUserId) // <-- LEBIH BAIK DARI whereNotEqualTo
                    .whereEqualTo("isRead", false)
                    .get().await()

                val batch: WriteBatch = db.batch()
                for (doc in unreadMessagesQuery.documents) {
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit().await()
                Log.d("ChatVM", "Menandai ${unreadMessagesQuery.size()} pesan sebagai sudah dibaca.")
            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal menandai pesan sebagai sudah dibaca", e)
            }
        }
    }

    fun sendMessage(chatRoomId: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null || messageText.value.isBlank() || chatRoomId.isBlank()) return

        val participants = chatRoomId.split("_")
        val otherUserId = participants.firstOrNull { it != currentUserId }
        if (otherUserId == null) {
            Log.e("ChatVM", "Gagal mengirim pesan, otherUserId tidak ditemukan.")
            return
        }

        val messageData = mapOf(
            "text" to messageText.value,
            "senderId" to currentUserId,
            "timestamp" to Timestamp.now(),
            "isRead" to false
        )

        val textToSend = messageText.value
        messageText.value = ""

        viewModelScope.launch {
            val chatRoomRef = db.collection("chats").document(chatRoomId)
            try {
                chatRoomRef.collection("messages").add(messageData).await()

                val updatedParticipants = listOf(currentUserId, otherUserId)
                val chatRoomData = mapOf(
                    "participants" to updatedParticipants,
                    "lastMessageTimestamp" to messageData["timestamp"] as Timestamp,
                    "lastMessageText" to textToSend
                )
                chatRoomRef.set(chatRoomData, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal mengirim pesan", e)
                messageText.value = textToSend
            }
        }
    }
}