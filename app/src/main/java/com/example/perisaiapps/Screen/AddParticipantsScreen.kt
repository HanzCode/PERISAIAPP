package com.example.perisaiapps.ui.screen.mentor

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.perisaiapps.viewmodel.AddGroupParticipantsViewModel
import com.example.perisaiapps.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParticipantsScreen(
    chatId: String,
    navController: NavController,
    addViewModel: AddGroupParticipantsViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val users by addViewModel.users.collectAsState()
    var selectedUsers by remember { mutableStateOf(setOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(key1 = chatId) {
        // Ambil dulu data chat yang ada untuk mendapatkan daftar peserta saat ini
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("chats").document(chatId).get()
            .addOnSuccessListener { doc ->
                val participants = doc.get("participants") as? List<String> ?: emptyList()
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
                    if (selectedUsers.isEmpty()) {
                        Toast.makeText(context, "Pilih minimal satu anggota untuk ditambahkan", Toast.LENGTH_SHORT).show()
                        return@FloatingActionButton
                    }
                    isLoading = true
                    // Panggil fungsi yang benar dari ChatViewModel
                    chatViewModel.addParticipantsToGroup(chatId, selectedUsers.toList()) {
                        isLoading = false
                        navController.popBackStack() // Kembali ke halaman chat setelah selesai
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
                        onCheckedChange = null
                    )
                    Text(user.displayName, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}