package com.example.perisaiapps.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perisaiapps.Model.Lomba
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InfoLombaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _lombaList = MutableStateFlow<List<Lomba>>(emptyList())
    val lombaList = _lombaList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        fetchLombaList()
    }

    fun fetchLombaList() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val snapshot = db.collection("Lomba").get().await()
                _lombaList.value = snapshot.documents.mapNotNull {
                    it.toObject(Lomba::class.java)?.copy(id = it.id)
                }
            } catch (e: Exception) {
                Log.e("InfoLombaViewModel", "Error fetching lomba list", e)
                _errorMessage.value = "Gagal memuat data lomba."
            } finally {
                _isLoading.value = false
            }
        }
    }
}