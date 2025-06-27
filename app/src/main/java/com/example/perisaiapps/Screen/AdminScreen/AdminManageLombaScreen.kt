package com.example.perisaiapps.Screen.AdminScreen

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Lomba
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// --- Palet Warna (konsisten dengan tema gelap) ---
private val darkBackground = Color(0xFF120E26)
private val cardBackground = Color(0xFF1F1A38)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val accentColor = Color(0xFF8A2BE2)
private val errorColor = Color(0xFFD50000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageLombaScreen(navController: NavController) {
    var lombaList by remember { mutableStateOf<List<Lomba>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var lombaToDelete by remember { mutableStateOf<Lomba?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        isLoading = true
        FirebaseFirestore.getInstance().collection("Lomba")
            .orderBy("namaLomba", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                lombaList = result.documents.mapNotNull { it.toObject(Lomba::class.java)?.copy(id = it.id) }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                errorMessage = "Gagal mengambil data: ${exception.message}"
                isLoading = false
            }
    }

    val deleteLombaAction: (String) -> Unit = { lombaId ->
        FirebaseFirestore.getInstance().collection("Lomba").document(lombaId).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Lomba berhasil dihapus", Toast.LENGTH_SHORT).show()
                lombaList = lombaList.filterNot { it.id == lombaId }
                lombaToDelete = null
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "Gagal menghapus: ${error.message}", Toast.LENGTH_LONG).show()
                lombaToDelete = null
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Informasi Lomba", color = textColorPrimary) },
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
                onClick = { navController.navigate("add_edit_lomba") },
                containerColor = accentColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Tambah Lomba Baru")
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
            Text("Daftar Lomba", style = MaterialTheme.typography.titleLarge, color = textColorPrimary, modifier = Modifier.padding(top = 8.dp))
            Text("Tambah, ubah, atau hapus informasi lomba.", style = MaterialTheme.typography.bodyMedium, color = textColorSecondary)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cari lomba...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColorPrimary,
                    unfocusedTextColor = textColorPrimary,
                    cursorColor = accentColor,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = textColorSecondary,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = textColorSecondary,
                    focusedContainerColor = cardBackground
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = accentColor) }
                errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: $errorMessage", color = errorColor) }
                else -> {
                    val filteredList = lombaList.filter { it.namaLomba.contains(searchQuery, ignoreCase = true) }
                    if (filteredList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(if (searchQuery.isNotBlank()) "Lomba tidak ditemukan" else "Belum ada data lomba", color = textColorSecondary)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(filteredList, key = { it.id }) { lomba ->
                                AdminLombaListItem(
                                    lomba = lomba,
                                    onEditClick = { navController.navigate("add_edit_lomba?lombaId=${lomba.id}") },
                                    onDeleteClick = { lombaToDelete = lomba }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (lombaToDelete != null) {
            DeleteConfirmationDialog(
                itemName = lombaToDelete!!.namaLomba,
                onConfirm = { deleteLombaAction(lombaToDelete!!.id) },
                onDismiss = { lombaToDelete = null }
            )
        }
    }
}

@Composable
private fun AdminLombaListItem(lomba: Lomba, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = lomba.imageUrl,
                contentDescription = "Poster ${lomba.namaLomba}",
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(lomba.namaLomba, fontWeight = FontWeight.Bold, color = textColorPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(lomba.penyelenggara, color = textColorSecondary, style = MaterialTheme.typography.bodySmall)
                Text("Deadline: ${lomba.pendaftaran}", color = textColorSecondary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            Row {
                IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, "Edit", tint = textColorSecondary) }
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, "Hapus", tint = errorColor) }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(itemName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, "Peringatan", tint = errorColor) },
        title = { Text("Konfirmasi Hapus") },
        text = { Text("Apakah Anda yakin ingin menghapus '$itemName'? Aksi ini tidak dapat dibatalkan.") },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = errorColor)) { Text("Hapus") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}