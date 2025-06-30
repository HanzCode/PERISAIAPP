package com.example.perisaiapps.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val messageText = mutableStateOf("")

    fun getMessages(chatRoomId: String) = callbackFlow {
        if (chatRoomId.isBlank()) {
            close()
            return@callbackFlow
        }
        val messagesCollection = db.collection("chats").document(chatRoomId).collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listener = messagesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ChatVM", "Gagal mendengarkan pesan", error)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Anotasi @DocumentId di ChatMessage akan bekerja di sini
                val messages = snapshot.toObjects(ChatMessage::class.java)
                trySend(messages).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }


    fun sendMessage(chatRoomId: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null || messageText.value.isBlank() || chatRoomId.isBlank()) {
            return
        }

        val participants = chatRoomId.split("_")
        val otherUserId = participants.firstOrNull { it != currentUserId }

        if (otherUserId == null) {
            Log.e("ChatVM", "Tidak bisa menemukan ID lawan bicara dari chatRoomId: $chatRoomId")
            return
        }

        // =====================================================================
        // BAGIAN PALING PENTING: KITA MEMBUAT MAP, BUKAN OBJEK CHATMESSAGE
        // =====================================================================
        val messageData = mapOf(
            "text" to messageText.value,
            "senderId" to currentUserId,
            "timestamp" to Timestamp.now()
            // Perhatikan: TIDAK ADA field 'id' di sini. Ini kuncinya.
        )
        // =====================================================================

        val textToSend = messageText.value
        messageText.value = "" // Reset field input di UI

        viewModelScope.launch {
            val chatRoomRef = db.collection("chats").document(chatRoomId)
            try {
                // Mengirim Map ke Firestore, bukan objek
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
                messageText.value = textToSend // Kembalikan teks jika gagal
            }
        }
    }
}