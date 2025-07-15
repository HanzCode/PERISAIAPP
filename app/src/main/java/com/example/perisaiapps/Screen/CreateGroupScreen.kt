package com.example.perisaiapps.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Mentor
import com.example.perisaiapps.Model.User
import com.example.perisaiapps.viewmodel.CreateGroupViewModel
import com.example.perisaiapps.viewmodel.GroupCreationState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateGroupScreen(
    navController: NavController,
    viewModel: CreateGroupViewModel = viewModel()
) {
    val context = LocalContext.current
    val mentors by viewModel.mentors.collectAsState()
    val users by viewModel.users.collectAsState()
    val creationState by viewModel.creationState.collectAsState()

    LaunchedEffect(key1 = creationState) {
        when(val state = creationState) {
            is GroupCreationState.Success -> {
                Toast.makeText(context, "Grup berhasil dibuat!", Toast.LENGTH_SHORT).show()
                // Navigasi ke grup baru dan hapus layar ini dari backstack
                navController.navigate("detail_chat/${state.newChatId}") {
                    popUpTo("create_group_screen") { inclusive = true }
                }
                viewModel.clearCreationState()
            }
            is GroupCreationState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.clearCreationState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Grup Baru") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.createGroup() }) {
                Icon(Icons.Default.Check, "Buat Grup")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = viewModel.groupName.value,
                    onValueChange = { viewModel.groupName.value = it },
                    label = { Text("Nama Grup") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Text("Pilih Satu Mentor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(mentors, key = { it.id }) { mentor ->
                SelectableUserRow(
                    name = mentor.name,
                    photoUrl = mentor.photoUrl,
                    isSelected = viewModel.selectedMentorId.value == mentor.userId,
                    isRadio = true,
                    onSelect = { viewModel.selectedMentorId.value = mentor.userId }
                )
            }

            item { Text("Pilih Teman (minimal 1)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(users, key = { it.userId }) { user ->
                SelectableUserRow(
                    name = user.displayName,
                    photoUrl = user.photoUrl,
                    isSelected = user.userId in viewModel.selectedUserIds.value,
                    isRadio = false,
                    onSelect = {
                        val currentSelection = viewModel.selectedUserIds.value.toMutableSet()
                        if (user.userId in currentSelection) {
                            currentSelection.remove(user.userId)
                        } else {
                            currentSelection.add(user.userId)
                        }
                        viewModel.selectedUserIds.value = currentSelection
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectableUserRow(
    name: String,
    photoUrl: String,
    isSelected: Boolean,
    isRadio: Boolean, // Tentukan apakah ini RadioButton atau Checkbox
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = photoUrl.ifBlank { null },
            contentDescription = "Foto $name",
            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary),
            contentScale = ContentScale.Crop
        )
        Text(text = name, modifier = Modifier.weight(1f).padding(horizontal = 16.dp))
        if (isRadio) {
            RadioButton(selected = isSelected, onClick = onSelect)
        } else {
            Checkbox(checked = isSelected, onCheckedChange = { onSelect() })
        }
    }
}