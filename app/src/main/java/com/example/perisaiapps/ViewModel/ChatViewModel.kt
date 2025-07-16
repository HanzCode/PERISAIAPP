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
import com.example.perisaiapps.Model.ChatRoom
import com.example.perisaiapps.Model.SharedNote
import com.example.perisaiapps.Model.User
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.functions
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
import kotlin.jvm.java

class ChatViewModel (application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val functions = Firebase.functions("asia-southeast2")

    private val _chatRoomDetails = MutableStateFlow<ChatRoom?>(null)
    val chatRoomDetails = _chatRoomDetails.asStateFlow()

    private val _participantProfiles = MutableStateFlow<List<User>>(emptyList())
    val participantProfiles = _participantProfiles.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messageText = mutableStateOf("")
    val messages = _messages.asStateFlow()

    private val _notes = MutableStateFlow<List<SharedNote>>(emptyList())
    val notes = _notes.asStateFlow()

    private val _mentorshipStatus = MutableStateFlow("ACTIVE") // Status: ACTIVE, COMPLETED
    val mentorshipStatus = _mentorshipStatus.asStateFlow()

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
    fun listenForMentorshipStatus(chatId: String) {
        // Fungsi ini hanya relevan untuk chat privat (P2P) yang ID-nya gabungan UID
        if (!chatId.contains("_")) {
            _mentorshipStatus.value = "GROUP_CHAT" // Atau status lain untuk menandakan ini grup
            return
        }

        // Untuk chat P2P, ID chat sama dengan ID permintaan
        val requestDocRef = db.collection("mentorship_requests").document(chatId)

        requestDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ChatVM", "Gagal listen ke status bimbingan.", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status")
                if (status == "COMPLETED") {
                    _mentorshipStatus.value = "COMPLETED"
                } else {
                    _mentorshipStatus.value = "ACTIVE"
                }
            } else {
                // Jika dokumen request tidak ditemukan (misal, untuk grup), anggap aktif
                _mentorshipStatus.value = "ACTIVE"
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
    fun addParticipantsToGroup(chatId: String, newUserIds: List<String>, onComplete: () -> Unit) {
        if (newUserIds.isEmpty()) {
            onComplete()
            return
        }

        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val chatRoomRef = db.collection("chats").document(chatId)
            try {
                // Ambil data chat saat ini untuk memeriksa tipe & nama
                val currentChatDoc = chatRoomRef.get().await()
                val currentType = currentChatDoc.getString("type")

                val updates = mutableMapOf<String, Any>()

                // Jika ini adalah chat privat ("DIRECT") yang pertama kali diubah jadi grup
                if (currentType == "DIRECT") {
                    updates["type"] = "GROUP"
                    if (currentChatDoc.getString("groupName").isNullOrBlank()) {
                        updates["groupName"] = "Grup Diskusi" // Beri nama default
                    }
                }

                // Gunakan FieldValue.arrayUnion untuk menambahkan anggota baru tanpa duplikasi
                updates["participants"] = FieldValue.arrayUnion(*newUserIds.toTypedArray())
                updates["lastActivityTimestamp"] = Timestamp.now()

                // Tambahkan pesan sistem bahwa anggota baru telah ditambahkan
                val addedUsersText = "Admin/User menambahkan anggota baru." // Anda bisa buat ini lebih detail
                updates["lastMessageText"] = addedUsersText

                // Jalankan update
                chatRoomRef.update(updates).await()

                // Kirim pesan sistem ke sub-koleksi messages
                val systemMessage = mapOf(
                    "text" to addedUsersText,
                    "type" to "SYSTEM", // Tipe pesan baru untuk styling berbeda jika perlu
                    "timestamp" to Timestamp.now(),
                    "senderId" to currentUserId
                )
                chatRoomRef.collection("messages").add(systemMessage).await()

                onComplete() // Panggil callback jika sukses

            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal menambahkan peserta", e)
                onComplete() // Tetap panggil callback agar UI tidak stuck
            }
        }
    }
    fun loadChatRoomAndParticipants(chatRoomId: String) {
        if (chatRoomId.isBlank()) return
        // Listener untuk dokumen chat utama
        db.collection("chats").document(chatRoomId).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val room = snapshot.toObject(ChatRoom::class.java)
            _chatRoomDetails.value = room

            // Jika ada peserta, ambil profil mereka
            room?.participants?.let { ids ->
                fetchParticipantProfiles(ids)
            }
        }
    }

    fun updateGroupName(chatId: String, newName: String) {
        if (newName.isBlank()) return
        val chatRoomRef = db.collection("chats").document(chatId)
        viewModelScope.launch {
            chatRoomRef.update("groupName", newName).await()
        }
    }

    fun updateGroupPhoto(chatId: String, imageUri: Uri) {
        viewModelScope.launch {
            val imageUrl = uploadToCloudinary(imageUri)
            if (imageUrl != null) {
                db.collection("chats").document(chatId).update("groupPhotoUrl", imageUrl).await()
            }
        }
    }

    private fun fetchParticipantProfiles(participantIds: List<String>) {
        if (participantIds.isEmpty()) return
        viewModelScope.launch {
            try {
                // Ambil semua profil user/mentor dalam satu kueri 'whereIn' yang efisien
                val result = db.collection("users").whereIn("userId", participantIds).get().await()
                _participantProfiles.value = result.toObjects(User::class.java)
            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal mengambil profil peserta", e)
            }
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

            // Ambil daftar peserta dari state yang sudah ada, BUKAN dari chatId.
            val currentParticipants = _chatRoomDetails.value?.participants
            if (currentParticipants.isNullOrEmpty()) {
                Log.e("ChatVM", "Gagal mengirim, daftar peserta kosong.")
                if(localId != null) updateMessageStatus(localId, "FAILED")
                return
            }

            // Cari semua lawan bicara (bisa lebih dari satu untuk grup)
            val otherUserIds = currentParticipants.filter { it != currentUserId }

            viewModelScope.launch {
                val chatRoomRef = db.collection("chats").document(chatRoomId)
                // Gunakan ID lokal jika ada (untuk Optimistic UI), jika tidak, buat ID baru.
                val newMessageRef = if(localId != null) chatRoomRef.collection("messages").document(localId) else chatRoomRef.collection("messages").document()

                try {
                    db.runBatch { batch ->
                        batch.set(newMessageRef, messageData)
                        val lastMessageText = when(messageData["type"] as String) {
                            "IMAGE" -> "[Gambar]"
                            "FILE" -> "[File] ${messageData["fileName"]}"
                            else -> messageData["text"] as String
                        }

                        val chatRoomUpdateData = mutableMapOf<String, Any>()
                        chatRoomUpdateData["lastActivityTimestamp"] = messageData["timestamp"]!!
                        chatRoomUpdateData["lastMessageText"] = lastMessageText

                        // Naikkan unreadCount untuk SEMUA peserta lain
                        otherUserIds.forEach { otherId ->
                            chatRoomUpdateData["unreadCounts.${otherId}"] = FieldValue.increment(1)
                        }

                        batch.update(chatRoomRef, chatRoomUpdateData)
                    }.await()
                    // Hapus pesan sementara dari daftar lokal setelah berhasil disimpan
                    if (localId != null) {
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
            Toast.makeText(context, "Mempersiapkan unduhan...", Toast.LENGTH_SHORT).show()
            withContext(Dispatchers.IO) {
                try {
                    // 1. Ekstrak public_id dari URL asli
                    val publicId = url.substringAfterLast("/").substringBeforeLast(".")
                    if (publicId.isBlank()) throw Exception("Public ID tidak valid dari URL.")

                    // 2. Panggil Cloud Function (ini adalah operasi jaringan)
                    Log.d("Download", "Meminta signed URL untuk publicId: $publicId")
                    val result = functions.getHttpsCallable("getSignedCloudinaryUrl")
                        .call(mapOf("publicId" to publicId)).await()

                    val signedUrl = (result.data as? Map<*, *>)?.get("downloadUrl") as? String
                    if (signedUrl.isNullOrBlank()) throw Exception("Gagal mendapatkan URL unduhan dari server.")

                    Log.d("Download", "Signed URL diterima. Memulai unduhan...")

                    // 3. Unduh file dengan OkHttp (ini juga operasi jaringan)
                    val sanitizedFileName = fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                    val cacheDir = context.cacheDir
                    val file = File(cacheDir, sanitizedFileName)
                    val client = OkHttpClient()
                    val request = Request.Builder().url(signedUrl).build()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        response.body?.use { body ->
                            FileOutputStream(file).use { outputStream ->
                                outputStream.write(body.bytes())
                            }
                        }

                        // 4. Buka file (perlu pindah ke Main Thread)
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
                        Toast.makeText(
                            context,
                            "Gagal membuka file. Coba lagi.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    fun createGroupChat(
        initialParticipants: List<String>,
        onGroupCreated: (newChatId: String) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Buat dokumen baru dengan ID acak di koleksi 'chats'
            val newChatRoomRef = db.collection("chats").document()

            val groupName = "Grup Diskusi" // Nama default, bisa diubah nanti
            val initialSystemMessage = "Grup telah dibuat."

            val chatRoomData = mapOf(
                "participants" to initialParticipants.distinct(),
                "type" to "GROUP",
                "groupName" to groupName,
                "createdBy" to currentUserId,
                "lastActivityTimestamp" to Timestamp.now(),
                "lastMessageText" to initialSystemMessage,
                "unreadCounts" to initialParticipants.associateWith { 0 } // Set unread count semua anggota jadi 0
            )

            val systemMessageData = mapOf(
                "text" to initialSystemMessage,
                "type" to "SYSTEM",
                "senderId" to currentUserId,
                "timestamp" to Timestamp.now()
            )

            try {
                // Gunakan batch untuk menjalankan semua operasi sekaligus
                db.runBatch { batch ->
                    // 1. Buat dokumen chat grup utama
                    batch.set(newChatRoomRef, chatRoomData)
                    // 2. Tambahkan pesan sistem pertama
                    batch.set(newChatRoomRef.collection("messages").document(), systemMessageData)
                }.await()

                // Panggil callback dengan ID grup baru agar UI bisa navigasi
                onGroupCreated(newChatRoomRef.id)

            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal membuat grup chat baru", e)
            }
        }
    }
    fun completeMentorship(chatId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // Untuk chat P2P, chatId adalah gabungan menteeId dan mentorId
                // yang juga merupakan ID dari dokumen permintaan.
                val requestDocRef = db.collection("mentorship_requests").document(chatId)

                // Update status menjadi COMPLETED
                requestDocRef.update("status", "COMPLETED").await()

                // Anda juga bisa menambahkan pesan sistem ke chat jika mau
                // ...

                onComplete() // Panggil callback jika sukses untuk navigasi kembali
            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal menyelesaikan bimbingan", e)
                // Handle error jika perlu, misal dengan Toast
            }
        }
    }
    fun leaveGroup(chatId: String, onLeaveSuccess: () -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return
        // Ambil nama pengguna saat ini dari data yang sudah kita muat
        val currentUserProfile = _participantProfiles.value.find { it.userId == currentUserId }
        val currentUserName = currentUserProfile?.displayName ?: "Seseorang"

        viewModelScope.launch {
            val chatRoomRef = db.collection("chats").document(chatId)
            val leaveMessage = "$currentUserName telah keluar dari grup."

            try {
                // Gunakan WriteBatch untuk menjalankan semua operasi secara atomik
                db.runBatch { batch ->
                    // 1. Hapus ID pengguna dari array 'participants'
                    batch.update(chatRoomRef, "participants", FieldValue.arrayRemove(currentUserId))

                    // 2. Kirim pesan sistem ke chat
                    val systemMessageData = mapOf(
                        "text" to leaveMessage,
                        "type" to "SYSTEM",
                        "senderId" to currentUserId,
                        "timestamp" to Timestamp.now()
                    )
                    batch.set(chatRoomRef.collection("messages").document(), systemMessageData)

                    // 3. Perbarui pesan terakhir dan timestamp di dokumen utama
                    batch.update(chatRoomRef, mapOf(
                        "lastActivityTimestamp" to Timestamp.now(),
                        "lastMessageText" to leaveMessage
                    ))
                }.await()

                // Panggil callback jika semua operasi berhasil
                onLeaveSuccess()

            } catch (e: Exception) {
                Log.e("ChatVM", "Gagal keluar dari grup", e)
                // Tampilkan Toast atau pesan error jika perlu
            }
        }
    }

}