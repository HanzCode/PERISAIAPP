package com.example.perisaiapps.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.perisaiapps.Model.User
import com.example.perisaiapps.viewmodel.AddGroupParticipantsViewModel
import com.example.perisaiapps.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParticipantsScreen(
    chatId: String,
    navController: NavController,
    addViewModel: AddGroupParticipantsViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    // Ambil peserta yang sudah ada dari chat lama untuk difilter
    // Ini asumsi chatId adalah gabungan UID, perlu disesuaikan jika grup sudah ada
    // Cara lebih baik adalah meneruskan list participants dari layar sebelumnya.
    // Untuk saat ini, kita sederhanakan.
    val currentParticipants = remember { mutableStateListOf<String>() } // Akan diisi dari DB

    val users by addViewModel.users.collectAsState()
    var selectedUsers by remember { mutableStateOf(setOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = chatId) {
        // Ambil dulu data chat yang ada untuk mendapatkan daftar peserta saat ini
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("chats").document(chatId).get()
            .addOnSuccessListener { doc ->
                val participants = doc.get("participants") as? List<String> ?: emptyList()
                currentParticipants.addAll(participants)
                addViewModel.loadUsers(participants) // Muat user KECUALI yang sudah jadi peserta
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Anggota") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    isLoading = true
                    val allParticipants = currentParticipants + selectedUsers.toList()
                    chatViewModel.createGroupChat(allParticipants) { newChatId ->
                        isLoading = false
                        // Navigasi ke grup chat yang BARU dibuat,
                        navController.popBackStack("detail_chat/$chatId", true)
                        navController.navigate("detail_chat/$newChatId")
                    }
                }
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Icon(Icons.Default.Check, "Tambah Anggota")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(users, key = { it.userId }) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = user.userId in selectedUsers,
                            onClick = {
                                selectedUsers = if (user.userId in selectedUsers) {
                                    selectedUsers - user.userId
                                } else {
                                    selectedUsers + user.userId
                                }
                            }
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = user.userId in selectedUsers,
                        onCheckedChange = null // Aksi sudah ditangani oleh Row
                    )
                    Text(user.displayName, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}