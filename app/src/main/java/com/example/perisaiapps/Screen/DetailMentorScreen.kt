package com.example.perisaiapps.Screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Mentor // Pastikan import model Mentor
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

// Helper function untuk format Timestamp (bisa ditaruh di file util terpisah)
fun formatTimestamp(timestamp: Timestamp?, pattern: String = "dd MMMM yyyy, HH:mm"): String {
    return timestamp?.toDate()?.let { date ->
        SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    } ?: "Tidak ditentukan"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMentorScreen(navController: NavController, mentorId: String) {
    var mentorDetail by remember { mutableStateOf<Mentor?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(mentorId) {
        if (mentorId.isNotEmpty()) {
            isLoading = true
            errorMessage = null
            Log.d("DetailMentorScreen", "Fetching detail for Mentor ID: $mentorId")
            FirebaseFirestore.getInstance().collection("Mentor").document(mentorId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        try {
                            // Jika model Mentor pakai @DocumentId, id akan terisi otomatis
                            // Jika tidak, pastikan .copy(id = document.id) dipakai saat mapping awal
                            mentorDetail = document.toObject(Mentor::class.java)
                            Log.d("DetailMentorScreen", "Data mentor ditemukan: ${mentorDetail?.name}")
                        } catch (e: Exception) {
                            Log.e("DetailMentorScreen", "Error mapping document to Mentor: ${e.message}", e)
                            errorMessage = "Gagal memproses data detail mentor."
                        }
                    } else {
                        Log.w("DetailMentorScreen", "Document not found for Mentor ID: $mentorId")
                        errorMessage = "Data mentor tidak ditemukan."
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    Log.e("DetailMentorScreen", "Error getting mentor details: ${exception.message}", exception)
                    errorMessage = "Gagal mengambil detail mentor: ${exception.message}"
                    isLoading = false
                }
        } else {
            Log.e("DetailMentorScreen", "Received empty Mentor ID.")
            errorMessage = "ID Mentor tidak valid."
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = mentorDetail?.name ?: "Detail Mentor",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B1533) // Warna tema Anda
                )
            )
        },
        containerColor = Color(0xFF1B1533) // Warna tema Anda
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                mentorDetail != null -> {
                    val mentor = mentorDetail!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally // Pusatkan konten di Column
                    ) {
                        // Tampilkan Foto Mentor
                        if (!mentor.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = mentor.photoUrl,
                                contentDescription = "Foto ${mentor.name}",
                                modifier = Modifier
                                    .size(150.dp) // Ukuran foto mentor
                                    .clip(CircleShape) // Buat foto menjadi lingkaran
                                    .background(Color.Gray.copy(alpha = 0.3f)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Placeholder jika tidak ada foto
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Tidak Ada Foto", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Tampilkan Nama Mentor (sudah ada di TopAppBar, tapi bisa juga di sini)
                        // Text(
                        //     text = mentor.name,
                        //     fontSize = 24.sp,
                        //     fontWeight = FontWeight.Bold,
                        //     color = Color.White,
                        //     textAlign = TextAlign.Center
                        // )
                        // Spacer(modifier = Modifier.height(8.dp))

                        // Card untuk detail lainnya
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2342))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                MentorDetailTextItem(label = "Peminatan", value = mentor.peminatan)
                                MentorDetailTextItem(label = "Deskripsi", value = mentor.Deskripsi, isLongText = true)
                                MentorDetailTextItem(label = "Tersedia Hingga", value = formatTimestamp(mentor.availableUntil))
                                // Tambahkan detail lain jika ada
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Anda bisa menambahkan tombol aksi di sini, misal "Hubungi Mentor"
                        // Button(onClick = { /* TODO: Aksi hubungi mentor */ }) {
                        //     Text("Hubungi Mentor")
                        // }
                    }
                }
                else -> {
                    Text(
                        text = "Detail mentor tidak dapat ditampilkan.",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MentorDetailTextItem(label: String, value: String?, isLongText: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold, // Sedikit lebih tebal untuk label
            color = Color.White.copy(alpha = 0.7f), // Warna abu-abu untuk label
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "N/A",
            fontSize = 16.sp,
            color = Color.White,
            maxLines = if (isLongText) Int.MAX_VALUE else 5, // Lebih banyak baris untuk deskripsi
            overflow = if (isLongText) TextOverflow.Visible else TextOverflow.Ellipsis
        )
    }
}