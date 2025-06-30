package com.example.perisaiapps.ui.screen.mentor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.perisaiapps.viewmodel.ChatViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun NotesScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    // Gunakan ViewModel yang sama dengan DetailChatScreen
    viewModel: ChatViewModel = viewModel()
) {
    // Ambil data catatan dari ViewModel
    val notes by viewModel.notes.collectAsState()
    var noteText by remember { mutableStateOf("") }

    // Update TextField saat data dari Firestore masuk
    LaunchedEffect(notes) {
        notes.firstOrNull()?.let {
            noteText = it.text
        }
    }

    // Ambil catatan saat layar pertama kali dibuka
    LaunchedEffect(chatId) {
        viewModel.getNotes(chatId)

        // Simpan otomatis saat user berhenti mengetik selama 1 detik
        snapshotFlow { noteText }
            .debounce(1000) // Tunggu 1 detik setelah ketikan terakhir
            .collect { text ->
                if(text != notes.firstOrNull()?.text) { // Hanya simpan jika ada perubahan
                    viewModel.upsertNote(chatId, text)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catatan Bersama") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxSize(),
                label = { Text("Tulis catatan di sini...") }
            )
        }
    }
}