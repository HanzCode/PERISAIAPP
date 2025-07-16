package com.example.perisaiapps.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.perisaiapps.Component.GreetingSection
import com.example.perisaiapps.Component.HorizontalLombaSection
import com.example.perisaiapps.Component.PeminatanSection
import com.example.perisaiapps.ViewModel.HomeViewModel

private val darkBackground = Color(0xFF120E26)

@Composable
fun HomeScreen(
    bottomNavController: NavController,
    rootNavController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    // Ambil semua state dari ViewModel
    val userProfile by viewModel.userProfile.collectAsState()
    val lombaList by viewModel.lombaList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        if (isLoading && userProfile == null) {
            // Tampilan loading awal
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorMessage != null) {
            // Simpan ke variabel lokal yang stabil
            val currentError = errorMessage
            // Tampilan error
            Text(
                text = currentError.toString(),
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else {
            // Tampilan konten utama
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp)
            ) {
                // Header sapaan
                GreetingSection(
                    name = userProfile?.displayName ?: "Pengguna",
                    photoUrl = userProfile?.photoUrl ?: "",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section Lomba
                HorizontalLombaSection(
                    lombaList = lombaList,
                    navController = rootNavController,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Section Peminatan
                PeminatanSection(
                    navController = bottomNavController,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}