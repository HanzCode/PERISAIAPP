package com.example.perisaiapps.ui.screen.mentor

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.perisaiapps.ui.theme.PerisaiAppsTheme
import com.example.perisaiapps.viewmodel.EditMentorProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMentorProfileScreen(
    mentorId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditMentorProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val mentor by viewModel.mentor.collectAsState()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    LaunchedEffect(key1 = mentorId) {
        if (mentorId.isNotBlank()) {
            viewModel.loadMentorProfile(mentorId)
        }
    }

    LaunchedEffect(key1 = updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        // 6. Panggil fungsi saveChanges dari ViewModel
                        IconButton(onClick = { viewModel.saveChanges() }) {
                            Icon(Icons.Default.Check, contentDescription = "Simpan", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Jika ViewModel sedang mengambil data awal, tampilkan loading
        if (mentor == null && isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Jika data sudah ada, tampilkan form
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val displayImage: Any? = viewModel.newImageUri.value ?: mentor?.photoUrl
                item {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = displayImage?.toString()
                                ?.ifBlank { "https://example.com/placeholder.jpg" },
                            contentDescription = "Foto Profil",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Ubah Foto",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item {
                    FormTextField(
                        value = viewModel.name.value,
                        onValueChange = { viewModel.name.value = it },
                        label = "Nama Lengkap"
                    )
                }
                item {
                    FormTextField(
                        value = viewModel.peminatan.value,
                        onValueChange = { viewModel.peminatan.value = it },
                        label = "Bidang Peminatan"
                    )
                }
                item {
                    FormTextField(
                        value = viewModel.deskripsi.value,
                        onValueChange = { viewModel.deskripsi.value = it },
                        label = "Deskripsi Diri",
                        singleLine = false,
                        modifier = Modifier.defaultMinSize(minHeight = 120.dp)
                    )
                }
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bersedia menerima mentee", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            Switch(
                                checked = viewModel.bersediaKah.value,
                                onCheckedChange = { viewModel.bersediaKah.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pencapaian", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(viewModel.achievements.value.size) { index ->
                    val achievement = viewModel.achievements.value[index]
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(text = "• $achievement", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        IconButton(onClick = { viewModel.removeAchievement(index) }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus pencapaian", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    var newAchievementText by remember { mutableStateOf("") }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FormTextField(
                            value = newAchievementText,
                            onValueChange = { newAchievementText = it },
                            label = "Pencapaian baru",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            viewModel.addAchievement(newAchievementText)
                            newAchievementText = ""
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Pencapaian", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// Helper Composable untuk konsistensi tampilan TextField
@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Preview
@Composable
private fun EditMentorProfileScreenPreview() {
    PerisaiAppsTheme {
        EditMentorProfileScreen(mentorId = "123", onNavigateBack = {})
    }
}