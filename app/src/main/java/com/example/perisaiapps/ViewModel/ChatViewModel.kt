package com.example.perisaiapps.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.perisaiapps.Model.ChatMessage
import com.example.perisaiapps.Model.SharedNote
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.coroutines.resume

class ChatViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val functions: FirebaseFunctions = Firebase.functions("asia-southeast2")

    val messageText = mutableStateOf("")
    private val _notes = MutableStateFlow<List<SharedNote>>(emptyList())
    val notes = _notes.asStateFlow()

    fun downloadAndOpenFile(context: Context, originalUrl: String, fileName: String) {
        viewModelScope.launch() { // Jalankan di thread IO untuk networking
            withContext(Dispatchers.IO) {
                Toast.makeText(context, "Mempersiapkan unduhan...", Toast.LENGTH_SHORT).show()
            }
            try {
                val publicId = originalUrl.substringAfterLast("/").substringBeforeLast(".")
                if (publicId.isBlank()) throw Exception("Public ID tidak valid.")

                // 2. Panggil Cloud Function     untuk mendapatkan URL yang ditandatangani
                Log.d("Download", "Meminta signed URL untuk publicId: $publicId")
                val result = functions.getHttpsCallable("getSignedCloudinaryUrl")
                    .call(mapOf("publicId" to publicId)).await()

                val signedUrl = (result.data as? Map<*, *>)?.get("downloadUrl") as? String
                if (signedUrl == null) throw Exception("Gagal mendapatkan URL unduhan dari server.")

                Log.d("Download", "Signed URL diterima: $signedUrl")

                // 3. Gunakan URL yang sudah ditandatangani untuk mengunduh dengan OkHttp
                val sanitizedFileName = fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                val cacheDir = context.cacheDir
                val file = File(cacheDir, sanitizedFileName)

                val client = OkHttpClient()
                val request = Request.Builder().url(signedUrl).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    // Tulis file dari response ke penyimpanan
                    response.body?.let { body ->
                        FileOutputStream(file).use { outputStream ->
                            outputStream.write(body.bytes())
                        }
                    }
                    val fileUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    val openIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(fileUri, context.contentResolver.getType(fileUri))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Unduhan selesai. Membuka file...", Toast.LENGTH_SHORT).show()
                        context.startActivity(openIntent)
                    }

                } else {
                    throw Exception("Gagal mengunduh: Server merespon dengan kode ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("Download", "Error saat mengunduh file secara manual", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal membuka file.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun uploadFileToCloudinary(uri: Uri, fileName: String? = null): String? {
        val publicId = UUID.randomUUID().toString()
        return suspendCancellableCoroutine { continuation ->
            val requestId = MediaManager.get().upload(uri)
                .unsigned("perisai_chat_files")
                .option("resource_type", "auto")
                .option("public_id", publicId)
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

    fun sendFileMessage(chatRoomId: String, fileUri: Uri, context: Context) {
        val currentUserId = auth.currentUser?.uid ?: return
        val participants = chatRoomId.split("_")
        val otherUserId = participants.firstOrNull { it != currentUserId } ?: return

        // Ambil nama file asli dari URI
        var fileName = "file"
        context.contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }

        viewModelScope.launch {
            // TODO: Tampilkan loading di UI jika perlu
            val fileUrl = uploadFileToCloudinary(fileUri, fileName)
            if (fileUrl == null) {
                Log.e("ChatVM", "Gagal mengirim file karena URL dari Cloudinary null")
                return@launch
            }

            val messageData = mapOf(
                "text" to fileName, // Gunakan text untuk menyimpan nama file
                "fileUrl" to fileUrl,
                "type" to "FILE",
                "fileName" to fileName,
                "senderId" to currentUserId,
                "timestamp" to Timestamp.now(),
                "isRead" to false
            )

            // Logika penyimpanan ke Firestore
            val chatRoomRef = db.collection("chats").document(chatRoomId)
            val newMessageRef = chatRoomRef.collection("messages").document()
            try {
                db.runBatch { batch ->
                    batch.set(newMessageRef, messageData)
                    val chatRoomUpdateData = mapOf(
                        "participants" to listOf(currentUserId, otherUserId),
                        "lastActivityTimestamp" to messageData["timestamp"] as Timestamp,
                        "lastMessageText" to "[File] $fileName",
                        "unreadCounts.${otherUserId}" to FieldValue.increment(1)
                    )
                    batch.update(chatRoomRef, chatRoomUpdateData)
                }.await()
            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal mengirim pesan file ke Firestore", e)
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
            val imageUrl = uploadFileToCloudinary(imageUri) // Gunakan fungsi yg sudah ada
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