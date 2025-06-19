package com.example.perisaiapps.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Lomba
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DetailLombaViewModel : ViewModel() {

    private val _lombaDetail = MutableStateFlow<Lomba?>(null)
    val lombaDetail = _lombaDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun fetchLombaDetail(lombaId: String) {
        if (lombaId.isBlank()) {
            _errorMessage.value = "ID Lomba tidak valid."
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val document = FirebaseFirestore.getInstance()
                    .collection("Lomba")
                    .document(lombaId)
                    .get()
                    .await()

                if (document != null && document.exists()) {
                    _lombaDetail.value = document.toObject(Lomba::class.java)?.copy(id = document.id)
                } else {
                    _errorMessage.value = "Data lomba tidak ditemukan."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal mengambil detail: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}