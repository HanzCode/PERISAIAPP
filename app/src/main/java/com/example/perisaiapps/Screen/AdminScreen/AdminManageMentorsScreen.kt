package com.example.perisaiapps.Screen.AdminScreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Mentor
import com.google.firebase.firestore.FirebaseFirestore


// --- Palet Warna (konsisten dengan tema gelap) ---
private val darkBackground = Color(0xFF120E26)
private val cardBackground = Color(0xFF1F1A38)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val accentColor = Color(0xFF8A2BE2)
private val successColor = Color(0xFF00C853)
private val errorColor = Color(0xFFD50000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageMentorsScreen(navController: NavController) {
    var mentorList by remember { mutableStateOf<List<Mentor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var mentorToDelete by remember { mutableStateOf<Mentor?>(null) }
    val context = LocalContext.current

    // Mengambil data mentor dari Firestore
    LaunchedEffect(Unit) {
        isLoading = true
        FirebaseFirestore.getInstance().collection("Mentor")
            .orderBy("name")
            .get()
            .addOnSuccessListener { result ->
                mentorList = result.documents.mapNotNull { it.toObject(Mentor::class.java) }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Log.e("AdminMentors", "Error getting documents: ", exception)
                errorMessage = "Gagal mengambil data: ${exception.message}"
                isLoading = false
            }
    }

    // Fungsi untuk menghapus mentor
    val deleteMentorAction: (String) -> Unit = { mentorId ->
        deleteMentor(
            mentorId = mentorId,
            onSuccess = {
                Toast.makeText(context, "Mentor berhasil dihapus", Toast.LENGTH_SHORT).show()
                mentorList = mentorList.filterNot { it.id == mentorId }
                mentorToDelete = null // Tutup dialog
            },
            onFailure = { error ->
                Toast.makeText(context, "Gagal menghapus: ${error.message}", Toast.LENGTH_LONG).show()
                mentorToDelete = null // Tutup dialog
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Mentor", color = textColorPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = textColorPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_edit_mentor") },
                containerColor = accentColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Tambah Mentor Baru")
            }
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Text(
                text = "Daftar Mentor Terdaftar",
                style = MaterialTheme.typography.titleLarge,
                color = textColorPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Ubah atau hapus data mentor dari sini.",
                style = MaterialTheme.typography.bodyMedium,
                color = textColorSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cari mentor...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    // --- Warna untuk Teks Input & Kursor ---
                    focusedTextColor = textColorPrimary,      // Warna teks saat diketik (fokus)
                    unfocusedTextColor = textColorPrimary,    // Warna teks saat tidak fokus
                    cursorColor = accentColor,              // Warna kursor

                    // --- Warna untuk Label, Border, Ikon ---
                    focusedBorderColor = accentColor,         // Warna border saat fokus
                    unfocusedBorderColor = textColorSecondary,  // Warna border saat tidak fokus
                    focusedLabelColor = accentColor,          // Warna label saat fokus
                    unfocusedLabelColor = textColorSecondary, // Warna label saat tidak fokus
                    focusedLeadingIconColor = accentColor,    // Warna ikon saat fokus
                    unfocusedLeadingIconColor = textColorSecondary, // Warna ikon saat tidak fokus

                    // Warna background text field
                    focusedContainerColor = cardBackground,
                    unfocusedContainerColor = cardBackground,
                    disabledContainerColor = cardBackground,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Konten utama (Loading, Error, Empty, List)
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = accentColor) }
                errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: $errorMessage", color = errorColor) }
                else -> {
                    val filteredList = mentorList.filter {
                        it.name.contains(searchQuery, ignoreCase = true) || it.peminatan.contains(searchQuery, ignoreCase = true)
                    }
                    if (filteredList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(if (searchQuery.isNotBlank()) "Mentor tidak ditemukan" else "Belum ada data mentor", color = textColorSecondary)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp) // Beri ruang untuk FAB
                        ) {
                            items(filteredList, key = { it.id }) { mentor ->
                                AdminMentorListItem(
                                    mentor = mentor,
                                    onEditClick = {
                                        navController.navigate("add_edit_mentor?mentorId=${mentor.id}")
                                    },
                                    onDeleteClick = {
                                        mentorToDelete = mentor // Tampilkan dialog konfirmasi
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog Konfirmasi Hapus
        if (mentorToDelete != null) {
            DeleteConfirmationDialog(
                mentorName = mentorToDelete!!.name,
                onConfirm = { deleteMentorAction(mentorToDelete!!.id) },
                onDismiss = { mentorToDelete = null }
            )
        }
    }
}

// Composable baru untuk item di daftar admin
@Composable
private fun AdminMentorListItem(
    mentor: Mentor,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
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
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(mentor.name, fontWeight = FontWeight.Bold, color = textColorPrimary, style = MaterialTheme.typography.bodyLarge)
                Text(mentor.peminatan, color = textColorSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (mentor.bersediaKah) "Bersedia" else "Sibuk",
                    color = if (mentor.bersediaKah) successColor else errorColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Tombol Aksi
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Mentor", tint = textColorSecondary)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus Mentor", tint = errorColor)
                }
            }
        }
    }
}

// Composable untuk dialog konfirmasi hapus
@Composable
private fun DeleteConfirmationDialog(
    mentorName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = "Peringatan", tint = errorColor) },
        title = { Text("Konfirmasi Hapus") },
        text = { Text("Apakah Anda yakin ingin menghapus mentor '$mentorName'? Aksi ini tidak dapat dibatalkan.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = errorColor)
            ) {
                Text("Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// Fungsi helper untuk menghapus dokumen mentor dari Firestore
private fun deleteMentor(mentorId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    // TODO: Implementasikan logika untuk menghapus foto mentor dari Cloudinary jika diperlukan.
    // Ini biasanya memerlukan backend atau Cloud Function untuk keamanan.
    FirebaseFirestore.getInstance().collection("Mentor").document(mentorId)
        .delete()
        .addOnSuccessListener {
            Log.d("FirestoreDelete", "Mentor document $mentorId successfully deleted.")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e("FirestoreDelete", "Error deleting mentor document $mentorId", e)
            onFailure(e)
        }
}