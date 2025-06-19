package com.example.perisaiapps.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Mentor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditMentorProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // State untuk menampung data mentor yang sedang diedit
    private val _mentor = MutableStateFlow<Mentor?>(null)
    val mentor = _mentor.asStateFlow()

    // State untuk setiap field yang bisa diubah
    val name = mutableStateOf("")
    val peminatan = mutableStateOf("")
    val deskripsi = mutableStateOf("")
    val bersediaKah = mutableStateOf(true)
    val achievements = mutableStateOf<List<String>>(emptyList())

    // State untuk UI (loading, success, error)
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess = _updateSuccess.asStateFlow()

    fun loadMentorProfile(mentorId: String) {
        if (mentorId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val document = db.collection("Mentor").document(mentorId).get().await()
                val mentorData = document.toObject(Mentor::class.java)
                _mentor.value = mentorData

                // Isi state dengan data yang ada
                mentorData?.let {
                    name.value = it.name
                    peminatan.value = it.peminatan
                    deskripsi.value = it.deskripsi
                    bersediaKah.value = it.bersediaKah
                    achievements.value = it.achievements ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAchievement(achievement: String) {
        if (achievement.isNotBlank()) {
            achievements.value = achievements.value + achievement
        }
    }

    fun removeAchievement(index: Int) {
        achievements.value = achievements.value.toMutableList().also { it.removeAt(index) }
    }

    fun saveChanges() {
        val mentorId = _mentor.value?.id ?: return
        if (mentorId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _updateSuccess.value = false

            // Buat map hanya dengan field yang diupdate
            val updatedData = mapOf(
                "name" to name.value,
                "peminatan" to peminatan.value,
                "deskripsi" to deskripsi.value,
                "bersediaKah" to bersediaKah.value,
                "achievements" to achievements.value
            )

            try {
                db.collection("Mentor").document(mentorId).update(updatedData).await()
                _updateSuccess.value = true // Tandai berhasil
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}