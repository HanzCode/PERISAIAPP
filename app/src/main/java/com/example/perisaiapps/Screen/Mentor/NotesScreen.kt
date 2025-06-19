package com.example.perisaiapps.ui.screen.mentor

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.perisaiapps.ui.theme.PerisaiAppsDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    chatId: String,
    onNavigateBack: () -> Unit
) {
    // Di sini Anda akan mengambil daftar catatan berdasarkan chatId dari ViewModel
    val menteeName = "Budi Santoso" // Data dummy

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catatan untuk $menteeName") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Buka dialog/halaman untuk menambah catatan baru */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Catatan")
            }
        }
    ) { paddingValues ->
        // TODO: Implementasi UI untuk menampilkan list catatan
        Text(text = "Daftar Catatan untuk chat ID: $chatId", modifier = androidx.compose.ui.Modifier.padding(paddingValues))
    }
}


@Preview
@Composable
private fun NotesScreenPreview() {
    PerisaiAppsDarkTheme {
        NotesScreen(chatId = "123", onNavigateBack = {})
    }
}