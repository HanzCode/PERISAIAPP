package com.example.perisaiapps.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.perisaiapps.Component.LombaListItem // <-- Gunakan komponen yang sudah modern
import com.example.perisaiapps.Model.Lomba
import com.example.perisaiapps.ViewModel.InfoLombaViewModel // <-- Import ViewModel baru

// --- Palet Warna untuk konsistensi ---
private val darkBackground = Color(0xFF120E26)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val accentColor = Color(0xFF8A2BE2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoLombaScreen(
    navController: NavController,
    viewModel: InfoLombaViewModel = viewModel() // <-- Gunakan ViewModel
) {
    // Ambil semua state dari ViewModel
    val lombaList by viewModel.lombaList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informasi Lomba", color = textColorPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Simpan pesan error ke variabel lokal untuk mengatasi smart cast
            val currentError = errorMessage
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = accentColor
                    )
                }
                currentError != null -> {
                    Text(
                        text = currentError, // Gunakan variabel lokal
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                lombaList.isEmpty() -> {
                    Text(
                        "Belum ada informasi lomba.",
                        color = textColorSecondary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(lombaList, key = { it.id }) { lomba ->
                            // Gunakan LombaListItem yang sudah kita perbaiki sebelumnya
                            LombaListItem(
                                lomba = lomba,
                                onItemClick = { clickedLombaId ->
                                    navController.navigate("detail_lomba/$clickedLombaId")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoLombaScreenPreview() {
    InfoLombaScreen(navController = rememberNavController())
}