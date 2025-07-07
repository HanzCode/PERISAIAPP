package com.example.perisaiapps.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.perisaiapps.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class EditUserProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile = _userProfile.asStateFlow()

    val displayName = mutableStateOf("")
    val newImageUri = mutableStateOf<Uri?>(null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess = _updateSuccess.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _isLoading.value = false
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val doc = db.collection("users").document(userId).get().await()
                val userData = doc.toObject(User::class.java)
                _userProfile.value = userData
                userData?.let {
                    displayName.value = it.displayName
                    newImageUri.value = null
                }
            } catch (e: Exception) {
                Log.e("EditUserVM", "Gagal memuat profil user", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        newImageUri.value = uri
    }

    private suspend fun uploadImageToCloudinary(uri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            val requestId = MediaManager.get().upload(uri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) { Log.d("EditUserVM_Cloudinary", "Upload dimulai...") }
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String
                        if (continuation.isActive) continuation.resume(secureUrl)
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("EditUserVM_Cloudinary", "Upload gagal: ${error.description}")
                        if (continuation.isActive) continuation.resume(null)
                    }
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()
            continuation.invokeOnCancellation { MediaManager.get().cancelRequest(requestId) }
        }
    }

    fun saveChanges() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _updateSuccess.value = false

            var uploadedPhotoUrl: String? = null
            // Panggil fungsi helper jika ada gambar baru
            if (newImageUri.value != null) {
                uploadedPhotoUrl = uploadImageToCloudinary(newImageUri.value!!)
            }

            val updatedData = mutableMapOf<String, Any>()
            updatedData["displayName"] = displayName.value

            // Hanya tambahkan photoUrl ke map jika berhasil diunggah
            if (uploadedPhotoUrl != null) {
                updatedData["photoUrl"] = uploadedPhotoUrl
            }

            // Hindari update jika tidak ada perubahan sama sekali
            if (updatedData.isEmpty()) {
                _isLoading.value = false
                return@launch
            }

            try {
                db.collection("users").document(userId).update(updatedData).await()
                _updateSuccess.value = true
            } catch (e: Exception) {
                Log.e("EditUserVM", "Gagal menyimpan perubahan ke Firestore", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}