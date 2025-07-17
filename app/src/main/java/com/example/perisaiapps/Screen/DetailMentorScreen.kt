package com.example.perisaiapps.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Mentor
import com.example.perisaiapps.viewmodel.DetailMentorViewModel
import com.google.firebase.auth.FirebaseAuth

private fun createChatId(uid1: String, uid2: String): String {
    return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMentorScreen(
    navController: NavController,
    mentorId: String,
    viewModel: DetailMentorViewModel = viewModel()
) {
    val mentor by viewModel.mentor.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mentorshipRequest by viewModel.mentorshipRequest.collectAsState()

    LaunchedEffect(key1 = mentorId) {
        viewModel.loadData(mentorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mentor?.name ?: "Detail Mentor", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                // Tentukan status dari objek. Jika objeknya null, anggap belum ada request.
                val status = mentorshipRequest?.status ?: "NOT_SENT"

                // Tampilkan loading jika data mentor belum dimuat
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else {
                    when (status) {
                        "NOT_SENT", "DECLINED", "COMPLETED" -> ActionButton(
                            text = "Kirim Permintaan Bimbingan",
                            onClick = { if (mentor != null) viewModel.sendMentorshipRequest(mentor!!) }
                        )
                        "PENDING" -> ActionButton(text = "Permintaan Terkirim", enabled = false)
                        "ACCEPTED" -> ActionButton(text = "Mulai Chat") {
                            if (mentor != null && currentUserId != null) {
                                val chatRoomId = createChatId(currentUserId, mentor!!.userId)
                                navController.navigate("detail_chat/$chatRoomId")
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Konten utama tidak berubah
        if (mentor != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { DetailMentorHeader(mentor = mentor!!) }
                item { DetailMentorInfoCard(title = "Tentang Mentor", content = mentor!!.deskripsi) }
                item { DetailMentorAchievementsCard(achievements = mentor!!.achievements ?: emptyList()) }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        } else if (!isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Gagal memuat profil mentor.")
            }
        }
    }
}
@Composable
private fun ActionButton(text: String, enabled: Boolean = true, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun DetailMentorHeader(mentor: Mentor) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = mentor.photoUrl.ifBlank { "https://example.com/placeholder.jpg" },
            contentDescription = "Foto profil ${mentor.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = mentor.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = mentor.peminatan, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailMentorInfoCard(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.tertiary)
            Text(text = content.ifBlank { "Informasi belum tersedia." }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun DetailMentorAchievementsCard(achievements: List<String>) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Pencapaian", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.tertiary)
            if (achievements.isEmpty()) {
                Text("Belum ada pencapaian yang ditambahkan.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                achievements.forEach { achievement ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                        Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(achievement, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}