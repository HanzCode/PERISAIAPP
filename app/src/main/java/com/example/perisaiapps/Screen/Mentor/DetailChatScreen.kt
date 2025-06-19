package com.example.perisaiapps.ui.screen.mentor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.perisaiapps.ui.theme.PerisaiAppsDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    onNavigateToNotes: () -> Unit
) {
    // Di sini Anda akan mengambil data chat berdasarkan chatId dari ViewModel
    val menteeName = "Budi Santoso" // Data dummy

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(menteeName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotes) {
                        Icon(Icons.Default.Note, contentDescription = "Lihat Catatan", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        // TODO: Implementasi UI untuk menampilkan list pesan dan input text
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Halaman Chat Detail untuk ID: $chatId")
        }
    }
}

@Preview
@Composable
private fun DetailChatScreenPreview() {
    PerisaiAppsDarkTheme {
        DetailChatScreen(chatId = "123", onNavigateBack = {}, onNavigateToNotes = {})
    }
}