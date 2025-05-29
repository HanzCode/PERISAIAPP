package com.example.perisaiapps.Screen.Mentor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorDashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mentor Dashboard") })
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
            Text("Selamat Datang, Mentor!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                // TODO: Navigasi ke halaman edit profil/deskripsi mentor
                // Contoh: navController.navigate("mentor_edit_profile_route")
            }) {
                Text("Edit Deskripsi Saya")
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(onClick = {
                FirebaseAuth.getInstance().signOut() // Logout
                navController.navigate("login") { // Kembali ke login
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }) {
                Text("Logout")
            }
        }
    }
}