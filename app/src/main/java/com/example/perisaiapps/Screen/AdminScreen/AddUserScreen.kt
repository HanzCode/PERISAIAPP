package com.example.perisaiapps.Screen.AdminScreen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.perisaiapps.viewmodel.AddUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    navController: NavController,
    viewModel: AddUserViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val addUserStatus by viewModel.addUserStatus.collectAsState()
    val context = LocalContext.current

    // LaunchedEffect untuk menangani hasil dari proses tambah user
    LaunchedEffect(key1 = addUserStatus) {
        val (isSuccess, message) = addUserStatus
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            if (isSuccess) {
                navController.popBackStack() // Kembali ke halaman sebelumnya jika sukses
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Pengguna Baru") },
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
            Text("Masukkan Detail Pengguna", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = viewModel.displayName.value,
                onValueChange = { viewModel.displayName.value = it },
                label = { Text("Nama Lengkap (Display Name)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.email.value,
                onValueChange = { viewModel.email.value = it },
                label = { Text("Alamat Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.password.value = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Pilih Peran (Role)", style = MaterialTheme.typography.titleMedium)

            val roles = listOf("user", "mentor", "admin")
            Row(modifier = Modifier.fillMaxWidth()) {
                roles.forEach { role ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (role == viewModel.role.value),
                                onClick = { viewModel.role.value = role },
                                role = Role.RadioButton
                            )
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (role == viewModel.role.value),
                            onClick = { viewModel.role.value = role }
                        )
                        Text(text = role.replaceFirstChar { it.uppercase() }, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.createUser() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Simpan Pengguna")
                }
            }
        }
    }
}