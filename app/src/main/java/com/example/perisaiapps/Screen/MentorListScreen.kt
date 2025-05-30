package com.example.perisaiapps.Screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.perisaiapps.Component.MentorItem
import com.example.perisaiapps.Model.Mentor
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MentorListScreen(navController: NavController) {
    // State untuk menyimpan list mentor
    var mentorList by remember { mutableStateOf<List<Mentor>>(emptyList()) }
    // State untuk menandakan sedang loading atau tidak
    var isLoading by remember { mutableStateOf(true) }
    // State untuk menyimpan pesan error jika ada
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // LaunchedEffect untuk mengambil data saat composable pertama kali dibuat
    LaunchedEffect(Unit) {
        isLoading = true // Mulai loading
        errorMessage = null // Reset error message
        FirebaseFirestore.getInstance().collection("Mentor")
            .get()
            .addOnSuccessListener { result ->
                try {
                    // Coba map data, gunakan mapNotNull untuk handle jika ada yg gagal di-map
                    val data = result.documents.mapNotNull { document ->
                        document.toObject(Mentor::class.java)?.copy(
                            // Jika Anda ingin menyimpan ID dokumen (opsional):
                             id = document.id
                        )
                    }
                    mentorList = data
                } catch (e: Exception) {
                    Log.e("Firestore", "Error mapping documents", e)
                    errorMessage = "Gagal memproses data mentor."
                } finally {
                    isLoading = false // Selesai loading (sukses atau gagal mapping)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
                errorMessage = "Gagal mengambil data: ${exception.message}"
                isLoading = false // Selesai loading karena gagal
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF161128)) // Warna background utama
            .padding(16.dp)
    ) {
        Text(
            "ALL MENTOR",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp, // Sedikit lebih besar
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.White)
        )

        // Tampilkan konten berdasarkan state
        Box(modifier = Modifier.fillMaxSize()) { // Box agar konten bisa di tengah
            when {
                // Jika sedang loading, tampilkan indikator progress
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White // Warna indikator loading
                    )
                }
                // Jika ada error, tampilkan pesan error
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Terjadi kesalahan",
                        color = Color.Red, // Warna teks error
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Jika data kosong setelah loading selesai
                mentorList.isEmpty() && !isLoading -> {
                    Text(
                        text = "Belum ada mentor yang tersedia.",
                        color = Color.Gray, // Warna teks info
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Jika data berhasil dimuat dan tidak kosong
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Beri jarak antar item
                    ) {
                        items(mentorList, key = { mentor -> mentor.id.takeIf { !it.isBlank() } ?: mentor.name }) { mentor ->
                            // Pastikan ID mentor tidak kosong sebelum membuat item bisa diklik untuk navigasi
                            if (!mentor.id.isBlank()) {
                                MentorItem(
                                    mentor = mentor,
                                    // ===== TAMBAHKAN LAMBDA UNTUK onItemClick DI SINI =====
                                    onItemClick = { mentorId -> // mentorId adalah mentor.id yang dikirim dari MentorItem
                                        Log.d("MentorListScreen", "MentorItem clicked, ID: $mentorId. Navigating...")
                                        try {
                                            // Gunakan navController yang sudah ada di MentorListScreen
                                            // (pastikan MentorListScreen menerima NavController dari MainScreen)
                                            navController.navigate("detail_mentor/$mentorId")
                                        } catch (e: Exception) {
                                            Log.e("MentorListScreen", "Navigation failed for mentor ID $mentorId: ${e.message}", e)
                                            // Anda bisa menambahkan Toast atau feedback lain ke pengguna jika navigasi gagal
                                        }
                                    }
                                    // =====================================================
                                )
                            } else {
                                // Jika ID mentor kosong, ini adalah masalah data atau logika fetch.
                                // Untuk sementara, tampilkan item tapi tidak bisa diklik atau log warning.
                                Log.w("MentorListScreen", "Mentor '${mentor.name}' has blank ID. Rendering non-clickable (or with no-op click).")
                                MentorItem(
                                    mentor = mentor,
                                    onItemClick = {
                                        // Tidak melakukan apa-apa atau log bahwa item tanpa ID diklik
                                        Log.w("MentorListScreen", "Clicked MentorItem for '${mentor.name}' which has a blank ID.")
                                    }
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}