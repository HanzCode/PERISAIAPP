package com.example.perisaiapps.ui.screen.mentor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.perisaiapps.viewmodel.MentorRequestsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    navController: NavController,
    viewModel: MentorRequestsViewModel = viewModel()
) {
    val requests by viewModel.requests.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Permintaan Bimbingan") }) }) { padding ->
        if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tidak ada permintaan baru.", modifier = Modifier.padding(padding))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(requests, key = { it.id }) { request ->
                    Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        ListItem(
                            headlineContent = { Text(request.menteeName) },
                            supportingContent = { Text("Ingin memulai sesi bimbingan.") },
                            leadingContent = { /* AsyncImage untuk foto mentee, ambil dari request.menteePhotoUrl */ },
                            trailingContent = {
                                Row {
                                    Button(onClick = { viewModel.acceptRequest(request) }) { Text("Terima") }
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedButton(onClick = { viewModel.declineRequest(request.id) }) { Text("Tolak") }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}