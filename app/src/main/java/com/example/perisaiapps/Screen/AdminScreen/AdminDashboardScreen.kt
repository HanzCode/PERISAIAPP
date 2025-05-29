package com.example.perisaiapps.Screen.AdminScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin Dashboard") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Selamat Datang, Admin!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                // Navigasi ke halaman tambah mentor (rute yang sudah dibuat sebelumnya)
                navController.navigate("add_edit_mentor")
            }) {
                Text("Kelola Mentor")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    // Navigasi ke halaman kelola mentor
                    navController.navigate("admin_manage_mentors_route") // Rute baru
                },
                modifier = Modifier.fillMaxWidth() // Buat tombol lebih lebar
            ) {
                Text("Kelola Mentor")
            }

            Button(
                onClick = {
                    // TODO: Navigasi ke halaman kelola lomba
                    navController.navigate("admin_manage_lomba_route") // Rute baru
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kelola Informasi Lomba")
            }
        }
    }
}