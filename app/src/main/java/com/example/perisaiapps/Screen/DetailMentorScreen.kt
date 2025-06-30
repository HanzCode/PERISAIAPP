package com.example.perisaiapps.Screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Mentor
import com.example.perisaiapps.viewmodel.DetailMentorViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMentorScreen(
    navController: NavController,
    mentorId: String,
    viewModel: DetailMentorViewModel = viewModel()
) {
    val mentor by viewModel.mentor.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showAvailabilityDialog by remember { mutableStateOf(false) }
    var isCreatingChat by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid


    LaunchedEffect(key1 = mentorId) {
        viewModel.fetchMentorDetail(mentorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mentor?.name ?: "Detail Mentor", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            mentor?.let {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f), // Sedikit transparan
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            if (it.bersediaKah) {
                                // Pastikan kedua ID ada sebelum membuat chat
                                if (currentUserId != null && it.userId.isNotBlank()) {
                                    isCreatingChat = true
                                    val chatRoomId = createChatId(currentUserId, it.userId)
                                    val chatRoomRef = db.collection("chats").document(chatRoomId)
                                    val participants = listOf(currentUserId, it.userId)
                                    coroutineScope.launch {
                                        try {
                                            // Buat atau update dokumen chat room dengan daftar peserta
                                            chatRoomRef.set(mapOf("participants" to participants), SetOptions.merge()).await()
                                            Log.d("DetailMentor", "Chat room $chatRoomId siap.")
                                            // Setelah berhasil, baru navigasi
                                            navController.navigate("detail_chat/$chatRoomId")
                                        } catch (e: Exception) {
                                            Log.e("DetailMentor", "Gagal membuat chat room", e)
                                            Toast.makeText(context, "Gagal memulai chat.", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isCreatingChat = false // Selesai loading tombol
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Gagal memulai chat, data tidak lengkap.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                showAvailabilityDialog = true
                            }
                        },
                        enabled = !isCreatingChat,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (isCreatingChat) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Hubungi Mentor", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },

        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                errorMessage != null -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center).padding(16.dp), textAlign = TextAlign.Center)
                mentor != null -> DetailMentorContent(mentor = mentor!!) // Panggil konten baru
            }
        }

        if (showAvailabilityDialog) {
            AlertDialog(
                onDismissRequest = { showAvailabilityDialog = false },
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                title = { Text(text = "Mentor Tidak Tersedia") },
                text = { Text(text = "Mentor ini sedang sibuk atau tidak menerima permintaan bimbingan saat ini. Silakan cari mentor lain.") },
                confirmButton = {
                    TextButton(onClick = { showAvailabilityDialog = false }) {
                        Text("Mengerti")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                iconContentColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// =====================================================================
// DI SINI KITA PERBAIKI LAYOUTNYA MENGGUNAKAN CARD
// =====================================================================

@Composable
fun DetailMentorContent(mentor: Mentor) {
    val colors = MaterialTheme.colorScheme

    // Ganti Column biasa dengan LazyColumn agar lebih efisien jika kontennya panjang
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Bagian Header ---
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = mentor.photoUrl.ifBlank { "https://example.com/placeholder.jpg" },
                    contentDescription = "Foto ${mentor.name}",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(colors.tertiary),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    // --- PERBAIKAN 1 ---
                    Text(
                        text = mentor.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    )
                    // --- PERBAIKAN 2 ---
                    Text(
                        text = mentor.peminatan,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.primary, // Warna aksen kuning
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // --- Card untuk Status Ketersediaan ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Status Saat Ini",
                        modifier = Modifier.weight(1f),
                        // --- PERBAIKAN 3 ---
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                    )
                    Text(
                        text = if (mentor.bersediaKah) "Bersedia" else "Sibuk",
                        color = if (mentor.bersediaKah) Color(0xFF00C853) else Color(0xFFD50000),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Card untuk "Tentang Mentor" ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tentang Mentor",
                        // --- PERBAIKAN 4 ---
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = colors.tertiary)
                    Text(
                        text = mentor.deskripsi,
                        // --- PERBAIKAN 5 ---
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = colors.onSurfaceVariant,
                            lineHeight = 24.sp
                        )
                    )
                }
            }
        }

        // --- Card untuk "Prestasi" ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Prestasi",
                        // --- PERBAIKAN 6 ---
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = colors.tertiary)

                    if (mentor.achievements.isNullOrEmpty()) {
                        Text(
                            text = "Belum ada prestasi yang dicantumkan.",
                            // --- PERBAIKAN 7 ---
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.onSurfaceVariant
                            )
                        )
                    } else {
                        mentor.achievements.forEach { achievement ->
                            Row(
                                modifier = Modifier.padding(bottom = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = colors.primary, // Warna aksen kuning
                                    modifier = Modifier.padding(end = 12.dp).size(20.dp).offset(y = 2.dp)
                                )
                                Text(
                                    text = achievement,
                                    // --- PERBAIKAN 8 ---
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = colors.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        // Spacer di akhir agar tidak terpotong oleh BottomAppBar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }

    }
}

private fun createChatId(uid1: String, uid2: String): String {
    return if (uid1 < uid2) {
        "${uid1}_${uid2}"
    } else {
        "${uid2}_${uid1}"
    }
}