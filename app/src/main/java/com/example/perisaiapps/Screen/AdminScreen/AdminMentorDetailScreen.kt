package com.example.perisaiapps.Screen.AdminScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.User
import com.example.perisaiapps.viewmodel.AdminMentorDetailViewModel
import com.example.perisaiapps.viewmodel.MentorshipHistory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMentorDetailScreen(
    navController: NavController,
    mentorId: String,
    viewModel: AdminMentorDetailViewModel = viewModel()
) {
    val mentor by viewModel.mentor.collectAsState()
    val ongoingMentees by viewModel.ongoingMentees.collectAsState()
    val completedHistory by viewModel.completedHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(key1 = mentorId) {
        viewModel.loadMentorData(mentorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mentor?.name ?: "Detail Mentor") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    mentor?.let {
                        Text(it.name, style = MaterialTheme.typography.headlineMedium)
                        Text(it.peminatan, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }

                item {
                    Text("Sedang Membimbing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                if (ongoingMentees.isEmpty()) {
                    item { Text("Saat ini tidak ada bimbingan yang sedang berjalan.") }
                } else {
                    items(ongoingMentees, key = { "ongoing_${it.userId}" }) { mentee ->
                        MenteeInfoCard(mentee = mentee)
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { Text("Riwayat Bimbingan Selesai", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

                // ========================================================
                // BAGIAN YANG HILANG DITAMBAHKAN DI SINI
                // ========================================================
                if (completedHistory.isEmpty()) {
                    item { Text("Belum ada riwayat bimbingan yang selesai.") }
                } else {
                    items(completedHistory, key = { "completed_${it.mentee.userId}" }) { historyItem ->
                        HistoryItemCard(historyItem = historyItem)
                    }
                }
            }
        }
    }
}

@Composable
private fun MenteeInfoCard(mentee: User) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(mentee.displayName, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(mentee.email) },
            leadingContent = {
                AsyncImage(
                    model = mentee.photoUrl,
                    contentDescription = "Foto ${mentee.displayName}",
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary),
                    contentScale = ContentScale.Crop
                )
            }
        )
    }
}

@Composable
private fun HistoryItemCard(historyItem: MentorshipHistory) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(historyItem.mentee.displayName, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(historyItem.mentee.email) },
            trailingContent = {
                Text(
                    // Ambil jumlah sesi dari data
                    text = "${historyItem.sessionCount} Sesi",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            leadingContent = {
                AsyncImage(
                    model = historyItem.mentee.photoUrl,
                    contentDescription = "Foto ${historyItem.mentee.displayName}",
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary),
                    contentScale = ContentScale.Crop
                )
            }
        )
    }
}