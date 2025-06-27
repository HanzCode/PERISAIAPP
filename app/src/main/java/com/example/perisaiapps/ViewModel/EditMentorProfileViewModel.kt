package com.example.perisaiapps.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.perisaiapps.Model.Mentor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class EditMentorProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    // DIHAPUS: Referensi ke Firebase Storage tidak diperlukan lagi

    private val _mentor = MutableStateFlow<Mentor?>(null)
    val mentor = _mentor.asStateFlow()

    // State untuk setiap field
    val name = mutableStateOf("")
    val peminatan = mutableStateOf("")
    val deskripsi = mutableStateOf("")
    val bersediaKah = mutableStateOf(true)
    val achievements = mutableStateOf<List<String>>(emptyList())
    val newImageUri = mutableStateOf<Uri?>(null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess = _updateSuccess.asStateFlow()

    // Fungsi untuk mengunggah gambar dan mendapatkan URL-nya
    private suspend fun uploadImageToCloudinary(uri: Uri): String? {
        // suspendCancellableCoroutine memungkinkan kita mengubah callback menjadi suspend function
        return suspendCancellableCoroutine { continuation ->
            // Simpan request ID agar bisa dibatalkan
            val requestId = MediaManager.get().upload(uri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("Cloudinary", "Upload dimulai...")
                    }
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String
                        Log.d("Cloudinary", "Upload berhasil: $secureUrl")
                        // Hanya lanjutkan jika coroutine masih aktif
                        if (continuation.isActive) {
                            continuation.resume(secureUrl)
                        }
                    }

                    // DIPERBAIKI: Tanda tangan (signature) fungsi dan parameter disesuaikan
                    override fun onError(requestId: String, error: ErrorInfo) {
                        // DIPERBAIKI: Menggunakan error.getDescription() untuk mendapatkan pesan error
                        Log.e("Cloudinary", "Upload gagal: ${error.description}")
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()

            // DIPERBAIKI: Menambahkan penanganan pembatalan (cancellation)
            continuation.invokeOnCancellation {
                // Jika coroutine dibatalkan, coba batalkan juga proses upload di Cloudinary
                MediaManager.get().cancelRequest(requestId)
                Log.d("Cloudinary", "Upload dibatalkan.")
            }
        }
    }

    fun saveChanges() {
        val mentorId = _mentor.value?.id ?: return
        if (mentorId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _updateSuccess.value = false

            var uploadedPhotoUrl: String? = null
            if (newImageUri.value != null) {
                uploadedPhotoUrl = uploadImageToCloudinary(newImageUri.value!!)
            }

            val updatedData = mutableMapOf<String, Any>(
                "name" to name.value,
                "peminatan" to peminatan.value,
                "deskripsi" to deskripsi.value,
                "bersediaKah" to bersediaKah.value,
                "achievements" to achievements.value
            )

            if (uploadedPhotoUrl != null) {
                updatedData["photoUrl"] = uploadedPhotoUrl
            }

            try {
                db.collection("Mentor").document(mentorId).update(updatedData).await()
                _updateSuccess.value = true
                Log.d("EditMentorVM", "Profil berhasil disimpan ke Firestore")
            } catch (e: Exception) {
                Log.e("EditMentorVM", "GAGAL menyimpan ke Firestore!", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Fungsi-fungsi lain tidak ada perubahan ---
    fun loadMentorProfile(mentorId: String) {
        if (mentorId.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val doc = db.collection("Mentor").document(mentorId).get().await()
                val mentorData = doc.toObject(Mentor::class.java)
                _mentor.value = mentorData
                mentorData?.let {
                    name.value = it.name
                    peminatan.value = it.peminatan
                    deskripsi.value = it.deskripsi
                    bersediaKah.value = it.bersediaKah
                    achievements.value = it.achievements ?: emptyList()
                    newImageUri.value = null
                }
            } catch (e: Exception) {
                Log.e("EditMentorVM", "Gagal memuat profil", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onImageSelected(uri: Uri) { newImageUri.value = uri }
    fun addAchievement(achievement: String) { if (achievement.isNotBlank()) { achievements.value = achievements.value + achievement } }
    fun removeAchievement(index: Int) { achievements.value = achievements.value.toMutableList().also { it.removeAt(index) } }
}