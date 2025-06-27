package com.example.perisaiapps.ViewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.perisaiapps.Model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    // ----------------------------------------------------

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "Pengguna tidak ditemukan."
            return
        }
        _isLoading.value = true
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                _userProfile.value = doc.toObject(UserProfile::class.java)
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Gagal memuat profil: ${e.message}"
                _isLoading.value = false
            }
    }

    fun updateProfile(displayName: String, selectedImageUri: Uri?, context: Context) {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true
        if (selectedImageUri != null) {
            uploadImageAndUpdateProfile(userId, displayName, selectedImageUri, context)
        } else {
            updateProfileData(userId, displayName, _userProfile.value?.photoUrl)
        }
    }

    private fun uploadImageAndUpdateProfile(uid: String, displayName: String, imageUri: Uri, context: Context) {
        val cloudName = "duaqqcjmr"
        val uploadPreset = "perisai_mentor"

        MediaManager.get().upload(imageUri).unsigned(uploadPreset).option("cloud_name", cloudName)
            .callback(object : UploadCallback {
                // --- PERBAIKAN 2: Tambahkan metode yang wajib ada ---
                override fun onStart(requestId: String?) {
                    Log.d("CloudinaryUpload", "Upload foto profil dimulai...")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    // Bisa digunakan untuk progress bar jika perlu
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.w("CloudinaryUpload", "Upload dijadwalkan ulang: ${error?.description}")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val newPhotoUrl = resultData["secure_url"] as? String
                    updateProfileData(uid, displayName, newPhotoUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    _errorMessage.value = "Upload foto gagal: ${error.description}"
                    _isLoading.value = false
                }
            }).dispatch(context)
    }

    private fun updateProfileData(uid: String, displayName: String, photoUrl: String?) {
        val updates = mapOf(
            "displayName" to displayName,
            "photoUrl" to (photoUrl ?: _userProfile.value?.photoUrl ?: "")
        )

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                _toastMessage.value = "Profil berhasil diperbarui!"
                fetchUserProfile() // Muat ulang data setelah update agar UI sinkron
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Gagal memperbarui profil: ${e.message}"
                _isLoading.value = false
            }
    }

    fun onToastShown() {
        _toastMessage.value = null
    }
}