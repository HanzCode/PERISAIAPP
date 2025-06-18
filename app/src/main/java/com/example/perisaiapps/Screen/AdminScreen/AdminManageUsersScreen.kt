package com.example.perisaiapps.Screen.AdminScreen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.perisaiapps.Model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// --- Palet Warna (konsisten dengan tema gelap) ---
private val darkBackground = Color(0xFF120E26)
private val cardBackground = Color(0xFF1F1A38)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val accentColor = Color(0xFF8A2BE2)
private val errorColor = Color(0xFFD50000)
private val adminRoleColor = Color(0xFFFDD835)
private val mentorRoleColor = Color(0xFF40C4FF)
private val userRoleColor = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageUsersScreen(navController: NavController) {
    var userList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var userToEditRole by remember { mutableStateOf<UserProfile?>(null) }
    var userToDelete by remember { mutableStateOf<UserProfile?>(null) }
    val context = LocalContext.current

    // Mengambil data pengguna dari Firestore
    LaunchedEffect(Unit) {
        isLoading = true
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("displayName", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                userList = result.documents.mapNotNull { it.toObject(UserProfile::class.java) }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                errorMessage = "Gagal mengambil data: ${exception.message}"
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Pengguna", color = textColorPrimary) },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = textColorPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
            )
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
            Text("Daftar Pengguna Aplikasi", style = MaterialTheme.typography.titleLarge, color = textColorPrimary, modifier = Modifier.padding(top = 8.dp))
            Text("Ubah peran atau hapus pengguna dari sistem.", style = MaterialTheme.typography.bodyMedium, color = textColorSecondary)
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cari pengguna...") },
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

            // Konten utama
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = accentColor) }
                errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: $errorMessage", color = errorColor) }
                else -> {
                    val filteredList = userList.filter {
                        it.displayName.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
                    }
                    if (filteredList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(if (searchQuery.isNotBlank()) "Pengguna tidak ditemukan" else "Tidak ada data pengguna", color = textColorSecondary)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredList, key = { it.uid }) { user ->
                                AdminUserListItem(
                                    user = user,
                                    onEditRoleClick = { userToEditRole = user },
                                    onDeleteClick = { userToDelete = user }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog untuk Mengubah Peran (ditampilkan jika userToEditRole tidak null)
        if (userToEditRole != null) {
            ChangeRoleDialog(
                user = userToEditRole!!,
                onDismiss = { userToEditRole = null },
                onRoleChange = { newRole ->
                    updateUserRole(
                        context = context,
                        uid = userToEditRole!!.uid,
                        newRole = newRole,
                        onSuccess = {
                            // Update list lokal agar UI langsung berubah
                            userList = userList.map { if (it.uid == userToEditRole!!.uid) it.copy(role = newRole) else it }
                            userToEditRole = null
                        }
                    )
                }
            )
        }

        // Dialog untuk Konfirmasi Hapus (ditampilkan jika userToDelete tidak null)
        if (userToDelete != null) {
            DeleteConfirmationDialog(
                userName = userToDelete!!.displayName,
                onConfirm = {
                    deleteUser(
                        context = context,
                        user = userToDelete!!,
                        onSuccess = {
                            userList = userList.filterNot { it.uid == userToDelete!!.uid }
                            userToDelete = null
                        }
                    )
                },
                onDismiss = { userToDelete = null }
            )
        }
    }
}

// --- Composable Helper untuk UI ---

@Composable
private fun AdminUserListItem(user: UserProfile, onEditRoleClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(user.displayName, fontWeight = FontWeight.Bold, color = textColorPrimary, style = MaterialTheme.typography.bodyLarge)
                Text(user.email, color = textColorSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.width(8.dp))
            RoleChip(role = user.role)
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onEditRoleClick) { Icon(Icons.Default.Edit, "Ubah Peran", tint = textColorSecondary) }
            IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, "Hapus Pengguna", tint = errorColor) }
        }
    }
}

@Composable
private fun RoleChip(role: String) {
    val (roleColor, roleText) = when (role.lowercase()) {
        "admin" -> adminRoleColor to "Admin"
        "mentor" -> mentorRoleColor to "Mentor"
        else -> userRoleColor to "User"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(roleColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(roleText, color = roleColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeRoleDialog(user: UserProfile, onDismiss: () -> Unit, onRoleChange: (String) -> Unit) {
    val roles = listOf("admin", "mentor", "user")
    var selectedRole by remember { mutableStateOf(user.role) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Edit, "Ubah Peran") },
        title = { Text("Ubah Peran untuk ${user.displayName}") },
        text = {
            Column {
                roles.forEach { role ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (role == selectedRole),
                                onClick = { selectedRole = role },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (role == selectedRole),
                            onClick = { selectedRole = role }
                        )
                        Text(text = role.replaceFirstChar { it.uppercase() }, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onRoleChange(selectedRole) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(userName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, "Peringatan", tint = errorColor) },
        title = { Text("Konfirmasi Hapus") },
        text = { Text("Apakah Anda yakin ingin menghapus data untuk pengguna '$userName'? Akun login mereka perlu dihapus manual dari Firebase Authentication.") },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = errorColor)) { Text("Hapus") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}


// --- Logika Backend (Fungsi Helper) ---

private fun updateUserRole(context: Context, uid: String, newRole: String, onSuccess: () -> Unit) {
    FirebaseFirestore.getInstance().collection("users").document(uid)
        .update("role", newRole)
        .addOnSuccessListener {
            Toast.makeText(context, "Peran pengguna berhasil diubah", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Gagal mengubah peran: ${it.message}", Toast.LENGTH_LONG).show()
        }
}

private fun deleteUser(context: Context, user: UserProfile, onSuccess: () -> Unit) {
    // PENTING: Menghapus dokumen di Firestore TIDAK menghapus akun login di Authentication.
    // Ini harus dilakukan manual di Firebase Console atau menggunakan Cloud Function untuk keamanan.
    Log.d("UserDelete", "Menghapus profil Firestore untuk user: ${user.uid}")
    Log.w("UserDelete", "INGAT: Hapus juga akun login untuk ${user.email} dari Firebase Authentication secara manual.")

    FirebaseFirestore.getInstance().collection("users").document(user.uid).delete()
        .addOnSuccessListener {
            // Jika user ini juga seorang mentor, hapus juga profilnya di koleksi 'Mentor'
            if (user.role == "mentor") {
                FirebaseFirestore.getInstance().collection("Mentor").whereEqualTo("userId", user.uid).get()
                    .addOnSuccessListener { mentorQuery ->
                        mentorQuery.documents.forEach { doc ->
                            Log.d("UserDelete", "Menghapus profil mentor terkait: ${doc.id}")
                            doc.reference.delete()
                        }
                    }
            }
            Toast.makeText(context, "Profil pengguna berhasil dihapus", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e("UserDelete", "Gagal menghapus profil pengguna.", e)
            Toast.makeText(context, "Gagal menghapus pengguna: ${e.message}", Toast.LENGTH_LONG).show()
        }
}