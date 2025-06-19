package com.example.perisaiapps.Screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.perisaiapps.viewmodel.UserRole
import com.example.perisaiapps.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

// Warna yang konsisten
private val darkBackground = Color(0xFF120E26)
private val cardBackground = Color(0xFF1F1A38)
private val accentColor = Color(0xFF8A2BE2)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)

@Composable
fun LoginScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val userRole by userViewModel.userRole
    val auth = FirebaseAuth.getInstance()

    // --- Logika Navigasi Otomatis ---
    // Efek ini memeriksa apakah sudah ada pengguna yang login saat layar pertama kali muncul.
    LaunchedEffect(key1 = Unit, key2 = auth.currentUser) {
        if (auth.currentUser != null && userRole == UserRole.UNKNOWN) {
            isLoading = true
            userViewModel.fetchUserRole()
        }
    }

    // Efek ini akan berjalan ketika `userRole` berubah.
    LaunchedEffect(key1 = userRole) {
        if (userRole != UserRole.UNKNOWN) {
            val destination = when (userRole) {
                UserRole.ADMIN -> "admin_dashboard_route"
                UserRole.MENTOR -> "mentor_dashboard_route"
                UserRole.USER -> "home"
                else -> null
            }
            destination?.let {
                navController.navigate(it) {
                    popUpTo("login") { inclusive = true }
                }
            }
            isLoading = false
        }
    }

    // --- Tampilan UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Hanya tampilkan form jika tidak ada proses loading otomatis
        if (!isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "App Logo",
                    tint = accentColor,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Selamat Datang Kembali", style = MaterialTheme.typography.headlineSmall, color = textColorPrimary)
                Text("Login untuk melanjutkan ke Perisai", style = MaterialTheme.typography.bodyMedium, color = textColorSecondary)
                Spacer(modifier = Modifier.height(32.dp))

                // Form Login
                LoginTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LoginTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                )

                loginErrorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            loginErrorMessage = "Email dan Password tidak boleh kosong."
                            return@Button
                        }
                        isLoading = true
                        loginErrorMessage = null
                        auth.signInWithEmailAndPassword(email.trim(), password)
                            .addOnSuccessListener {
                                userViewModel.fetchUserRole() // ViewModel akan memicu navigasi
                            }
                            .addOnFailureListener { exception ->
                                loginErrorMessage = when(exception) {
                                    is FirebaseAuthInvalidUserException -> "Email tidak terdaftar."
                                    is FirebaseAuthInvalidCredentialsException -> "Password salah."
                                    else -> "Login gagal, silakan coba lagi."
                                }
                                isLoading = false
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Login")
                }
            }
        }

        // Tampilkan loading indicator di tengah layar
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

// Helper untuk TextField agar konsisten
@Composable
private fun LoginTextField(
    value: String, onValueChange: (String) -> Unit, label: String, isPassword: Boolean = false,
    passwordVisible: Boolean = false, onPasswordVisibilityChange: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = onPasswordVisibilityChange) { Icon(image, "Toggle Password") }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColorPrimary,
            unfocusedTextColor = textColorPrimary,
            cursorColor = textColorPrimary,
            focusedBorderColor = accentColor,
            unfocusedBorderColor = textColorSecondary,
            focusedLabelColor = textColorPrimary,
            unfocusedLabelColor = textColorSecondary,
            focusedContainerColor = cardBackground,
            unfocusedContainerColor = cardBackground
        )
    )
}