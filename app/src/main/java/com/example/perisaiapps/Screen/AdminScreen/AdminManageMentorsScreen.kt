package com.example.perisaiapps.Screen.AdminScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Mentor
import com.example.perisaiapps.viewmodel.AdminManageMentorsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageMentorsScreen(
    navController: NavController,
    viewModel: AdminManageMentorsViewModel = viewModel()
) {
    val filteredMentorList by viewModel.filteredMentors.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var mentorToDelete by remember { mutableStateOf<Mentor?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Mentor") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_edit_mentor") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Tambah Mentor Baru")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Daftar Mentor Terdaftar",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                "Lihat detail, ubah, atau hapus data mentor.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = { Text("Cari mentor...") },
                leadingIcon = { Icon(Icons.Default.Search, "Cari") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: $errorMessage") }
                filteredMentorList.isEmpty() && searchQuery.isNotBlank() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Mentor tidak ditemukan") }
                filteredMentorList.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada data mentor") }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredMentorList, key = { it.id }) { mentor ->
                            AdminMentorListItem(
                                mentor = mentor,
                                onItemClick = { navController.navigate("admin_mentor_detail/${mentor.id}") },
                                onEditClick = { navController.navigate("add_edit_mentor?mentorId=${mentor.id}") },
                                onDeleteClick = { mentorToDelete = mentor }
                            )
                        }
                    }
                }
            }
        }

        // ========================================================
        // PERBAIKAN UTAMA ADA PADA PEMANGGILAN VIEWMODEL DI SINI
        // ========================================================
        if (mentorToDelete != null) {
            DeleteConfirmationDialog(
                mentorName = mentorToDelete!!.name,
                onConfirm = {
                    // Panggil fungsi hapus dari ViewModel, bukan fungsi lokal
                    viewModel.deleteMentor(
                        mentor = mentorToDelete!!,
                        onSuccess = { Toast.makeText(context, "Mentor berhasil dihapus", Toast.LENGTH_SHORT).show() },
                        onFailure = { errorMsg -> Toast.makeText(context, "Gagal menghapus: $errorMsg", Toast.LENGTH_LONG).show() }
                    )
                    mentorToDelete = null
                },
                onDismiss = { mentorToDelete = null }
            )
        }
    }
}

@Composable
private fun AdminMentorListItem(
    mentor: Mentor,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = mentor.photoUrl,
                contentDescription = "Foto ${mentor.name}",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiary),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(mentor.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(mentor.peminatan, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (mentor.bersediaKah) "Bersedia" else "Sibuk",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (mentor.bersediaKah) Color(0xFF00C853) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Row {
                IconButton(onClick = onItemClick) { Icon(Icons.Default.RemoveRedEye, "Edit Mentor") }
                IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, "Edit Mentor") }
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, "Hapus Mentor", tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    mentorName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, "Peringatan") },
        title = { Text("Konfirmasi Hapus") },
        text = { Text("Apakah Anda yakin ingin menghapus mentor '$mentorName'? Semua data terkait (termasuk akun login) akan dihapus.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Hapus") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}