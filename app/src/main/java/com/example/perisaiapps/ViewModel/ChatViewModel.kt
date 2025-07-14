package com.example.perisaiapps.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.coroutines.resume

class ChatViewModel (application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())

    val messageText = mutableStateOf("")
    val messages = _messages.asStateFlow()

    private val _notes = MutableStateFlow<List<SharedNote>>(emptyList())
    val notes = _notes.asStateFlow()


    fun getNotes(chatRoomId: String) {
        if (chatRoomId.isBlank()) return
        val notesCollection = db.collection("chats").document(chatRoomId).collection("notes")
            .orderBy("lastEdited", Query.Direction.DESCENDING)
        notesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot != null) {
                _notes.value = snapshot.toObjects(SharedNote::class.java)
            }
        }
    }

    fun upsertNote(chatRoomId: String, noteText: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val noteRef = db.collection("chats").document(chatRoomId).collection("notes").document("shared_note")
            val noteData = mapOf(
                "text" to noteText,
                "editorId" to currentUserId,
                "lastEdited" to Timestamp.now()
            )
            try {
                noteRef.set(noteData, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal menyimpan catatan", e)
            }
        }
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
            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal mereset unread count", e)
            }
        }
    }

    fun sendMessage(chatRoomId: String) {
        if (messageText.value.isBlank()) return
        val messageData = mapOf(
            "text" to messageText.value, "senderId" to auth.currentUser?.uid, "timestamp" to Timestamp.now(),
            "isRead" to false, "type" to "TEXT", "imageUrl" to null, "fileUrl" to null, "fileName" to null, "thumbnailUrl" to null
        )
        messageText.value = ""
        saveMessageToFirestore(chatRoomId, messageData)
    }

    fun listenForMessages(chatRoomId: String) {
        if (chatRoomId.isBlank()) return
        val messagesCollection = db.collection("chats").document(chatRoomId).collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        messagesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) { Log.e("ChatVM", "Listen error", error); return@addSnapshotListener }
            snapshot?.let {
                val firestoreMessages = it.toObjects(ChatMessage::class.java)
                _messages.update { currentMessages ->
                    val pendingMessages = currentMessages.filter { it.status != "SENT" }
                    (pendingMessages + firestoreMessages).distinctBy { it.id }.sortedByDescending { it.timestamp }
                }
            }
        }
    }

    private suspend fun uploadToCloudinary(data: Any): String? {
    return suspendCancellableCoroutine { continuation ->
        val request = when (data) {
            is Uri -> MediaManager.get().upload(data)
            is ByteArray -> MediaManager.get().upload(data)
            else -> { continuation.resume(null); return@suspendCancellableCoroutine }
        }
        request.unsigned("perisai_chat_files")
            .option("resource_type", "auto").option("public_id", UUID.randomUUID().toString())
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) { continuation.resume(resultData["secure_url"] as? String) }
                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("ChatVM_Cloudinary", "Upload gagal: ${error.description}")
                    continuation.resume(null)
                }
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }
}

    private suspend fun createPdfThumbnail(pdfUri: Uri): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val pfd: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(pdfUri, "r")
                pfd?.use {
                    val renderer = PdfRenderer(it)
                    val page = renderer.openPage(0)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    renderer.close()
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                    stream.toByteArray()
                }
            } catch (e: Exception) { Log.e("PdfThumbnail", "Gagal membuat thumbnail PDF", e); null }
        }
    }


    fun sendImageMessage(chatRoomId: String, imageUri: Uri) {
        val currentUserId = auth.currentUser?.uid ?: return
        val localId = UUID.randomUUID().toString()
        val tempMessage = ChatMessage(id = localId, type = "IMAGE", status = "UPLOADING", localUri = imageUri.toString(), senderId = currentUserId, timestamp = Timestamp.now())
        _messages.update { listOf(tempMessage) + it }

        viewModelScope.launch {
            val imageUrl = uploadToCloudinary(imageUri)
            if (imageUrl == null) {
                updateMessageStatus(localId, "FAILED"); return@launch
            }
            val messageData = mapOf(
                "text" to "[Gambar]", "imageUrl" to imageUrl, "fileUrl" to null, "fileName" to null, "thumbnailUrl" to null,
                "type" to "IMAGE", "senderId" to currentUserId, "timestamp" to tempMessage.timestamp, "isRead" to false
            )
            saveMessageToFirestore(chatRoomId, messageData, localId)
        }
    }
    private fun saveMessageToFirestore(chatRoomId: String, messageData: Map<String, Any?>, localId: String? = null) {
        val currentUserId = auth.currentUser?.uid ?: return
        val participants = chatRoomId.split("_")
        val otherUserId = participants.firstOrNull { it != currentUserId } ?: return

        viewModelScope.launch {
            val chatRoomRef = db.collection("chats").document(chatRoomId)
            val newMessageRef = chatRoomRef.collection("messages").document()
            try {
                db.runBatch { batch ->
                    batch.set(newMessageRef, messageData)
                    val lastMessageText = when(messageData["type"] as String) {
                        "IMAGE" -> "[Gambar]"
                        "FILE" -> "[File] ${messageData["fileName"]}"
                        else -> messageData["text"] as String
                    }
                    val chatRoomUpdateData = mapOf(
                        "participants" to listOf(currentUserId, otherUserId),
                        "lastActivityTimestamp" to messageData["timestamp"]!!,
                        "lastMessageText" to lastMessageText,
                        "unreadCounts.${otherUserId}" to FieldValue.increment(1)
                    )
                    batch.set(chatRoomRef, chatRoomUpdateData, SetOptions.merge())
                }.await()
                if (localId != null) {
                    // Cukup hapus dari list lokal, karena listener akan mengambil versi finalnya
                    _messages.update { it.filterNot { msg -> msg.id == localId } }
                }
            } catch (e: Exception) {
                if (localId != null) updateMessageStatus(localId, "FAILED")
                Log.e("ChatVM", "Gagal menyimpan pesan ke Firestore", e)
            }
        }
    }

    fun sendFileMessage(chatRoomId: String, fileUri: Uri) {
        val context = getApplication<Application>().applicationContext
        val currentUserId = auth.currentUser?.uid ?: return
        val localId = UUID.randomUUID().toString()
        var fileName = "file"
        context.contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) fileName = cursor.getString(nameIndex)
            }
        }
        val tempMessage = ChatMessage(id = localId, type = "FILE", fileName = fileName, status = "UPLOADING", localUri = fileUri.toString(), senderId = currentUserId, timestamp = Timestamp.now())
        _messages.update { listOf(tempMessage) + it }

        viewModelScope.launch {
            var thumbnailUrl: String? = null
            if (context.contentResolver.getType(fileUri) == "application/pdf") {
                val thumbnailBytes = createPdfThumbnail(fileUri)
                if (thumbnailBytes != null) {
                    thumbnailUrl = uploadToCloudinary(thumbnailBytes)
                }
            }
            val fileUrl = uploadToCloudinary(fileUri)
            if (fileUrl == null) {
                updateMessageStatus(localId, "FAILED"); return@launch
            }
            val messageData = mapOf(
                "text" to fileName, "imageUrl" to null, "fileUrl" to fileUrl, "fileName" to fileName, "thumbnailUrl" to thumbnailUrl,
                "type" to "FILE", "senderId" to currentUserId, "timestamp" to tempMessage.timestamp, "isRead" to false
            )
            saveMessageToFirestore(chatRoomId, messageData, localId)
        }
    }
    private fun updateMessageStatus(localId: String, newStatus: String, firestoreId: String? = null) {
        _messages.update { currentMessages ->
            currentMessages.map {
                if (it.id == localId) it.copy(id = firestoreId ?: it.id, status = newStatus) else it
            }
        }
    }


    fun downloadAndOpenFile(context: Context, url: String, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sanitizedFileName = fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                val downloadUrl = url.replace("/upload/", "/upload/fl_attachment/")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Memulai unduhan: $sanitizedFileName", Toast.LENGTH_SHORT).show()
                }
                val cacheDir = context.cacheDir
                val file = File(cacheDir, sanitizedFileName)
                val client = OkHttpClient()
                val request = Request.Builder().url(downloadUrl).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    response.body?.let { body ->
                        FileOutputStream(file).use { outputStream ->
                            outputStream.write(body.bytes())
                        }
                    }
                    val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    val openIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(fileUri, context.contentResolver.getType(fileUri))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    withContext(Dispatchers.Main) {
                        context.startActivity(openIntent)
                    }
                } else {
                    throw Exception("Gagal mengunduh: Server merespon dengan kode ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("Download", "Error saat mengunduh file secara manual", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal membuka file. Coba lagi.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}