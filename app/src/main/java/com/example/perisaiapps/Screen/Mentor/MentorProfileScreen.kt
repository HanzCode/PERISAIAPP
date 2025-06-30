package com.example.perisaiapps.ui.screen.mentor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Mentor
import com.example.perisaiapps.ui.theme.PerisaiAppsTheme
import com.example.perisaiapps.viewmodel.MentorProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorProfileScreen(
    // Navigasi ke halaman edit tetap dikelola oleh NavHost
    onNavigateToEdit: (String) -> Unit,
    viewModel: MentorProfileViewModel = viewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    // 1. Ambil state dari ViewModel
    val mentorProfile by viewModel.mentorProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Jika layar kembali aktif (misal setelah kembali dari halaman edit)
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya") },
                actions = {
                    // Hanya tampilkan tombol edit jika profil berhasil dimuat
                    mentorProfile?.id?.let { mentorId ->
                        if (mentorId.isNotBlank()) {
                            IconButton(onClick = { onNavigateToEdit(mentorId) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profil", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (isLoading) {
                // 2. Tampilkan loading indicator
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (mentorProfile != null) {
                // 3. Jika data ada, tampilkan konten profil
                ProfileContent(
                    mentor = mentorProfile!!,
                    onLogout = { viewModel.logout() } // Panggil fungsi logout dari ViewModel
                )
            } else {
                // Tampilkan pesan jika profil tidak ditemukan
                Text(
                    text = "Gagal memuat profil.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(mentor: Mentor, onLogout: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ProfileHeader(mentor = mentor)
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            ProfileInfoCard(title = "Tentang Saya", content = mentor.deskripsi)
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            ProfileAchievementsCard(achievements = mentor.achievements ?: emptyList())
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onLogout,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Logout",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}




@Composable
fun ProfileHeader(mentor: Mentor) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = mentor.photoUrl.ifBlank { "https://example.com/placeholder.jpg" }, // Gunakan photoUrl dari data
            contentDescription = "Foto profil ${mentor.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = mentor.name, // Gunakan nama dari data
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mentor.peminatan, // Gunakan peminatan dari data
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProfileInfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.tertiary)
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun ProfileAchievementsCard(achievements: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Pencapaian", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.tertiary)
            if (achievements.isEmpty()) {
                Text(
                    text = "Belum ada pencapaian yang ditambahkan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                achievements.forEach { achievement ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = achievement,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MentorProfileScreenPreview() {
    PerisaiAppsTheme {
        // Preview kini hanya butuh onNavigateToEdit
        MentorProfileScreen(onNavigateToEdit = {})
    }
}