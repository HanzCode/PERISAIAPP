package com.example.perisaiapps.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.perisaiapps.viewmodel.ChangePasswordResult
import com.example.perisaiapps.viewmodel.ChangePasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: ChangePasswordViewModel = viewModel()
) {
    val result by viewModel.changePasswordResult.collectAsState()
    val context = LocalContext.current

    // Efek untuk menampilkan hasil (Toast) dan navigasi kembali
    LaunchedEffect(key1 = result) {
        when (val res = result) {
            is ChangePasswordResult.Success -> {
                Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                viewModel.clearResultStatus()
                navController.popBackStack()
            }
            is ChangePasswordResult.Error -> {
                Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                viewModel.clearResultStatus()
            }
            else -> {} // Do nothing for Idle or Loading
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ganti Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Untuk keamanan, masukkan password lama Anda diikuti dengan password baru.", style = MaterialTheme.typography.bodyMedium)

            // Gunakan state dari ViewModel
            PasswordField(label = "Password Lama", value = viewModel.oldPassword.value, onValueChange = { viewModel.oldPassword.value = it })
            PasswordField(label = "Password Baru", value = viewModel.newPassword.value, onValueChange = { viewModel.newPassword.value = it })
            PasswordField(label = "Konfirmasi Password Baru", value = viewModel.confirmPassword.value, onValueChange = { viewModel.confirmPassword.value = it })

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.changePassword() },
                enabled = result != ChangePasswordResult.Loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (result is ChangePasswordResult.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Simpan Perubahan")
                }
            }
        }
    }
}

// Composable helper untuk field password
@Composable
private fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(image, contentDescription = "Toggle password visibility")
            }
        }
    )
}