package com.example.perisaiapps.Screen.AdminScreen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

// --- Palet Warna (konsisten dengan tema gelap) ---
private val darkBackground = Color(0xFF120E26)
private val cardBackground = Color(0xFF1F1A38)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val accentColor = Color(0xFF8A2BE2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(navController: NavController) {
    val context = LocalContext.current

    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Pengguna Baru", color = textColorPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = textColorPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Masukkan detail untuk pengguna baru dengan peran 'User'.",
                style = MaterialTheme.typography.bodyMedium,
                color = textColorSecondary
            )

            CustomUserTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = "Nama Lengkap",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
            )
            CustomUserTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Login",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            CustomUserTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password (min. 6 karakter)",
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (displayName.isBlank() || email.isBlank() || password.length < 6) {
                        Toast.makeText(context, "Semua field wajib diisi dan password minimal 6 karakter.", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    isLoading = true
                    createNewUser(
                        context = context,
                        displayName = displayName.trim(),
                        email = email.trim(),
                        password = password,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "Pengguna baru berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                            // Kembali ke halaman kelola pengguna dan refresh
                            navController.navigate("admin_manage_users_route") {
                                popUpTo("admin_manage_users_route") { inclusive = true }
                            }
                        },
                        onFailure = { error ->
                            isLoading = false
                            Toast.makeText(context, "Gagal: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Tambah Pengguna")
                }
            }
        }
    }
}

// Composable Helper untuk TextField di halaman ini
@Composable
private fun CustomUserTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(imageVector = image, contentDescription = "Toggle Password Visibility")
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColorPrimary,
            unfocusedTextColor = textColorPrimary,
            cursorColor = accentColor,
            focusedBorderColor = accentColor,
            unfocusedBorderColor = textColorSecondary,
            focusedLabelColor = accentColor,
            unfocusedLabelColor = textColorSecondary,
            focusedContainerColor = cardBackground,
            unfocusedContainerColor = cardBackground
        )
    )
}

// Fungsi untuk menangani logika pembuatan user
private fun createNewUser(
    context: Context,
    displayName: String,
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    // 1. Buat pengguna di Firebase Authentication
    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { authResult ->
            val newUserId = authResult.user?.uid
            if (newUserId == null) {
                onFailure(Exception("Gagal mendapatkan UID setelah membuat akun."))
                return@addOnSuccessListener
            }
            Log.d("AddUser", "1. Akun Auth berhasil dibuat. UID: $newUserId")

            // 2. Buat profil di koleksi 'users' dengan peran 'user'
            val userProfile = mapOf(
                "displayName" to displayName,
                "email" to email,
                "role" to "user" // Set peran default sebagai 'user'
            )

            FirebaseFirestore.getInstance().collection("users").document(newUserId)
                .set(userProfile)
                .addOnSuccessListener {
                    Log.d("AddUser", "2. Dokumen di 'users' berhasil dibuat.")
                    onSuccess()
                }
                .addOnFailureListener { firestoreError ->
                    Log.w("AddUser", "Gagal membuat profil di 'users'", firestoreError)
                    onFailure(firestoreError)
                }
        }
        .addOnFailureListener { authError ->
            val errorMessage = if (authError is FirebaseAuthUserCollisionException) {
                "Email ini sudah terdaftar. Silakan gunakan email lain."
            } else {
                authError.message ?: "Gagal membuat akun login."
            }
            Log.w("AddUser", "Gagal membuat akun Auth.", authError)
            onFailure(Exception(errorMessage))
        }
}