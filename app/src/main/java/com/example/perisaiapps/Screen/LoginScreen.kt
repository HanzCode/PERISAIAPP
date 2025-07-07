package com.example.perisaiapps.Screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.perisaiapps.R
import com.example.perisaiapps.viewmodel.LoginResult
import com.example.perisaiapps.viewmodel.LoginViewModel


@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val loginResult by viewModel.loginResult.collectAsState()
    val context = LocalContext.current

    // Efek ini akan berjalan ketika `loginResult` berubah
    LaunchedEffect(key1 = loginResult) {
        when (val result = loginResult) {
            is LoginResult.Success -> {
                // Tentukan tujuan berdasarkan role dari ViewModel
                val destination = when (result.role) {
                    "admin" -> "admin_dashboard_route"
                    "mentor" -> "mentor_main_route"
                    else -> "home"
                }
                navController.navigate(destination) {
                    popUpTo("login") { inclusive = true }
                    popUpTo("splash") { inclusive = true }
                }
                viewModel.clearLoginResult() // Reset status
            }
            is LoginResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.clearLoginResult() // Reset status
            }
            else -> { /* Do nothing for Idle or Loading */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Gunakan warna dari tema
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // GANTI Icon MENJADI Image
            Image(
                painter = painterResource(id = R.drawable.logo_perisai), // GANTI DENGAN NAMA FILE LOGO ANDA
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp) // Sesuaikan ukurannya
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Selamat Datang Kembali", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            Text("Login untuk melanjutkan ke Perisai", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))

            // Form Login
            LoginTextField(
                value = viewModel.email.value,
                onValueChange = { viewModel.email.value = it },
                label = "Email"
            )
            Spacer(modifier = Modifier.height(16.dp))
            var passwordVisible by remember { mutableStateOf(false) }
            LoginTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.password.value = it },
                label = "Password",
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.signIn() }, // Panggilan menjadi sangat sederhana
                enabled = loginResult != LoginResult.Loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (loginResult is LoginResult.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Login")
                }
            }
        }
    }
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: () -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                IconButton(onClick = onPasswordVisibilityChange) { Icon(image, "Toggle Password") }
            }
        },
        // Gunakan warna dari tema
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