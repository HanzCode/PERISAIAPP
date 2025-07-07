package com.example.perisaiapps.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class ChatViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val messageText = mutableStateOf("")
    private val _notes = MutableStateFlow<List<SharedNote>>(emptyList())
    val notes = _notes.asStateFlow()


    private suspend fun uploadImageToCloudinary(uri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            val requestId = MediaManager.get().upload(uri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) { Log.d("ChatVM_Cloudinary", "Upload gambar dimulai...") }
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String
                        Log.d("ChatVM_Cloudinary", "Upload gambar berhasil: $secureUrl")
                        if (continuation.isActive) {
                            continuation.resume(secureUrl)
                        }
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("ChatVM_Cloudinary", "Upload gambar gagal: ${error.description}")
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()

            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
                Log.d("ChatVM_Cloudinary", "Upload gambar dibatalkan.")
            }
        }
    }

    fun sendMessage(chatRoomId: String) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null || messageText.value.isBlank() || chatRoomId.isBlank()) return

        val participants = chatRoomId.split("_")
        val otherUserId = participants.firstOrNull { it != currentUserId } ?: return

        val textToSend = messageText.value
        val now = Timestamp.now()

        messageText.value = ""

        viewModelScope.launch {
            val chatRoomRef = db.collection("chats").document(chatRoomId)
            try {

                val initialChatData = mapOf("participants" to listOf(currentUserId, otherUserId))
                chatRoomRef.set(initialChatData, SetOptions.merge()).await()

                val messageData = mapOf(
                    "text" to textToSend,
                    "senderId" to currentUserId,
                    "timestamp" to now,
                    "isRead" to false
                )
                chatRoomRef.collection("messages").add(messageData).await()

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
    fun sendImageMessage(chatRoomId: String, imageUri: Uri) {
        val currentUserId = auth.currentUser?.uid ?: return
        val participants = chatRoomId.split("_")
        val otherUserId = participants.firstOrNull { it != currentUserId } ?: return

        viewModelScope.launch {
            // Tampilkan loading di UI jika perlu

            // 1. Unggah gambar ke Cloudinary
            val imageUrl = uploadImageToCloudinary(imageUri) // Gunakan fungsi yg sudah ada
            if (imageUrl == null) {
                Log.e("ChatVM", "Gagal mendapatkan URL gambar dari Cloudinary")
                // Tampilkan pesan error ke user jika perlu
                return@launch
            }

            // 2. Siapkan data pesan gambar
            val messageData = mapOf(
                "text" to "[Gambar]", // Teks placeholder untuk notifikasi/lastMessage
                "imageUrl" to imageUrl,
                "type" to "IMAGE",
                "senderId" to currentUserId,
                "timestamp" to Timestamp.now(),
                "isRead" to false
            )

            // 3. Simpan ke Firestore (logika mirip sendMessage)
            val chatRoomRef = db.collection("chats").document(chatRoomId)
            val newMessageRef = chatRoomRef.collection("messages").document()

            try {
                db.runBatch { batch ->
                    batch.set(newMessageRef, messageData)
                    val chatRoomUpdateData = mapOf(
                        "participants" to listOf(currentUserId, otherUserId),
                        "lastActivityTimestamp" to messageData["timestamp"] as Timestamp,
                        "lastMessageText" to "[Gambar]",
                        "unreadCounts.${otherUserId}" to FieldValue.increment(1)
                    )
                    batch.update(chatRoomRef, chatRoomUpdateData)
                }.await()
            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal mengirim pesan gambar", e)
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