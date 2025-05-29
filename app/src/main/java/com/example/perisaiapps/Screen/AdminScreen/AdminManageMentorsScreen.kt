package com.example.perisaiapps.Screen.AdminScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.perisaiapps.Component.MentorItem // Kita gunakan MentorItem yang sudah ada
import com.example.perisaiapps.Model.Mentor
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageMentorsScreen(navController: NavController) {
    var mentorList by remember { mutableStateOf<List<Mentor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        FirebaseFirestore.getInstance().collection("Mentor")
            .orderBy("name") // Urutkan berdasarkan nama, misalnya
            .get()
            .addOnSuccessListener { result ->
                try {
                    // Pastikan model Mentor Anda memiliki field 'id'
                    // dan diisi saat mapping (atau pakai @DocumentId)
                    val data = result.documents.mapNotNull { document ->
                        document.toObject(Mentor::class.java)?.apply {
                            // Jika model Mentor tidak menggunakan @DocumentId untuk field 'id',
                            // dan Anda perlu id dokumennya untuk operasi lain,
                            // Anda bisa membuat field 'docId' atau serupa di model, lalu:
                            // this.docId = document.id
                            // Namun, jika @DocumentId sudah ada di field 'id', ini sudah otomatis.
                        }
                    }
                    mentorList = data
                    Log.d("AdminMentors", "Mentors fetched: ${data.size}")
                } catch (e: Exception) {
                    Log.e("AdminMentors", "Error mapping documents", e)
                    errorMessage = "Gagal memproses data mentor."
                } finally {
                    isLoading = false
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AdminMentors", "Error getting documents: ", exception)
                errorMessage = "Gagal mengambil data: ${exception.message}"
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Mentor") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
                // Anda bisa menambahkan actions di sini jika perlu
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Navigasi ke layar tambah mentor
                // Pastikan rute "add_edit_mentor" sudah ada di NavGraph Anda
                navController.navigate("add_edit_mentor")
            }) {
                Icon(Icons.Filled.Add, "Tambah Mentor")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $errorMessage", color = Color.Red)
                }
            } else if (mentorList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada data mentor.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mentorList, key = { mentor -> mentor.id.ifBlank { UUID.randomUUID().toString() } }) { mentor ->
                        // Untuk saat ini, MentorItem mungkin tidak perlu onItemClick di sini,
                        // atau kliknya akan beda (misal, langsung ke edit)
                        // Kita gunakan MentorItem yang ada, tapi callback kliknya kita set kosong dulu
                        // atau kita bisa modifikasi MentorItem agar bisa menerima null untuk onItemClick
                        MentorItem(mentor = mentor, onItemClick = { mentorId ->
                            // TODO: Nanti, klik di sini akan navigasi ke layar edit mentor
                            Log.d("AdminMentors", "Mentor item clicked: $mentorId. Action: Edit/View Details")
                            navController.navigate("add_edit_mentor?mentorId=${mentorId}")
                        })
                    }
                }
            }
        }
    }
}