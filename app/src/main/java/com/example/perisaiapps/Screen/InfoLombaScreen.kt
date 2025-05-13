package com.example.perisaiapps.Screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.perisaiapps.Component.LombaItem
import com.example.perisaiapps.Model.Lomba
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// 1. Tambahkan parameter NavController
fun InfoLombaScreen(navController: NavController) {
    var lombaList by remember { mutableStateOf<List<Lomba>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // val uriHandler = LocalUriHandler.current // <-- 2. Hapus baris ini

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        FirebaseFirestore.getInstance().collection("Lomba")
            .get()
            .addOnSuccessListener { result ->
                try {
                    val data = result.documents.mapNotNull { document ->
                        // Pastikan ID disalin
                        document.toObject(Lomba::class.java)?.copy(id = document.id)
                    }
                    lombaList = data
                    Log.d("FirestoreLomba", "Data berhasil diambil: ${lombaList.size} items")
                } catch (e: Exception) {
                    Log.e("FirestoreLomba", "Error mapping documents", e)
                    errorMessage = "Gagal memproses data lomba."
                } finally {
                    isLoading = false
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreLomba", "Error getting documents: ", exception)
                errorMessage = "Gagal mengambil data: ${exception.message}"
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            // TopAppBar tetap sama
            TopAppBar(
                title = { Text("Informasi Lomba", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1533))
            )
        },
        containerColor = Color(0xFF1B1533)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> { /* CircularProgressIndicator */ }
                    errorMessage != null -> { /* Text Error */ }
                    lombaList.isEmpty() && !isLoading -> { /* Text Kosong */ }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                        ) {
                            // Gunakan fallback key jika id bisa null saat komposisi awal
                            items(lombaList, key = { lomba -> lomba.id ?: UUID.randomUUID().toString() }) { lomba ->
                                // Pastikan ID valid sebelum membuat item bisa diklik untuk navigasi
                                if (!lomba.id.isNullOrBlank()) {
                                    LombaItem(
                                        lomba = lomba,
                                        // ===== 3. UBAH ISI LAMBDA INI =====
                                        onItemClick = { clickedLombaId -> // Lambda ini menerima ID Lomba
                                            // Log sebelum navigasi
                                            Log.d("InfoLombaScreen", "Item clicked with ID: $clickedLombaId. Navigating...")
                                            try {
                                                // Gunakan NavController untuk navigasi
                                                navController.navigate("detail_lomba/$clickedLombaId")
                                            } catch (e: Exception) {
                                                // Tangani jika navigasi gagal
                                                Log.e("InfoLombaScreen", "Navigation error for ID $clickedLombaId: ${e.message}", e)
                                                // Bisa tambahkan Toast atau pesan error lain
                                            }
                                        }
                                        // ==================================
                                    )
                                } else {
                                    // Jika ID tidak valid, tampilkan item tapi tidak bisa diklik (lambda kosong)
                                    Log.w("InfoLombaScreen", "Lomba '${lomba.namaLomba}' has null/blank ID. Rendering non-clickable.")
                                    LombaItem(lomba = lomba, onItemClick = {})
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF1B1533)
@Composable
fun InfoLombaScreenPreview() {
    // 4. Sediakan NavController dummy untuk preview
    val previewNavController = rememberNavController()
    InfoLombaScreen(navController = previewNavController)
    // Alternatif: Tampilkan versi statis tanpa interaksi
    // Box(Modifier.fillMaxSize().background(Color(0xFF1B1533))) {
    //     Text("Preview InfoLombaScreen", color=Color.White, modifier=Modifier.align(Alignment.Center))
    // }
}