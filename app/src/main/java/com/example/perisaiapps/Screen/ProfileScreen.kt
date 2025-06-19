package com.example.perisaiapps.Screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.ViewModel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

// --- Palet Warna ---
private val darkBackground = Color(0xFF120E26)
private val cardBackground = Color(0xFF1F1A38)
private val accentColor = Color(0xFF8A2BE2)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val errorColor = Color(0xFFD50000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    // State untuk menampung perubahan sebelum disimpan
    var displayName by remember(userProfile) { mutableStateOf(userProfile?.displayName ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Menampilkan Toast dari ViewModel
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onToastShown()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya", color = textColorPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Bagian Header Profil ---
            ProfileHeader(
                name = userProfile?.displayName ?: "Pengguna",
                email = userProfile?.email ?: "Memuat...",
                photoUrl = selectedImageUri?.toString() ?: userProfile?.photoUrl ?: "",
                onImageClick = { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Bagian Edit Profil ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Edit Profil", style = MaterialTheme.typography.titleMedium, color = textColorSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProfileTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = "Nama Tampilan"
                        )
                        Button(
                            onClick = { viewModel.updateProfile(displayName, selectedImageUri, context) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Simpan Perubahan")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Bagian Menu Lainnya ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Akun", style = MaterialTheme.typography.titleMedium, color = textColorSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground)
                ) {
                    ProfileMenuItem(
                        text = "Logout",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        color = errorColor,
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- Composable Helper untuk kerapian UI ---

@Composable
private fun ProfileHeader(name: String, email: String, photoUrl: String, onImageClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(cardBackground)
                .border(2.dp, accentColor, CircleShape)
                .clickable(onClick = onImageClick),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto Profil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Placeholder Foto Profil",
                    modifier = Modifier.size(100.dp),
                    tint = textColorSecondary
                )
            }
        }
        Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = textColorPrimary)
        Text(text = email, style = MaterialTheme.typography.bodyMedium, color = textColorSecondary)
    }
}

@Composable
private fun ProfileTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            // Warna untuk Teks dan Kursor
            focusedTextColor = textColorPrimary,
            unfocusedTextColor = textColorPrimary,
            cursorColor = accentColor,

            // Warna untuk Border dan Label
            focusedBorderColor = accentColor,
            unfocusedBorderColor = textColorSecondary,
            focusedLabelColor = accentColor,
            unfocusedLabelColor = textColorSecondary,

            // Warna untuk state 'disabled' (seperti pada field email)
            disabledTextColor = textColorSecondary,
            disabledBorderColor = textColorSecondary.copy(alpha = 0.5f),
            disabledLabelColor = textColorSecondary
        )
    )
}

@Composable
private fun ProfileMenuItem(text: String, icon: ImageVector, color: Color = textColorPrimary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = color)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = color, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = textColorSecondary, modifier = Modifier.size(16.dp))
    }
}