package com.example.perisaiapps.Screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Lomba
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailLombaScreen(navController: NavController, lombaId: String) {
    var lombaDetail by remember { mutableStateOf<Lomba?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(lombaId) {
        // Pastikan ID tidak kosong sebelum fetch
        if (lombaId.isNotEmpty()) {
            isLoading = true
            errorMessage = null
            Log.d("DetailLombaScreen", "Fetching detail for Lomba ID: $lombaId")
            FirebaseFirestore.getInstance().collection("Lomba").document(lombaId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        try {
                            // Konversi ke Lomba dan sertakan ID dokumennya
                            lombaDetail =
                                document.toObject(Lomba::class.java)?.copy(id = document.id)
                            Log.d(
                                "DetailLombaScreen",
                                "Data detail ditemukan: ${lombaDetail?.namaLomba}"
                            )
                        } catch (e: Exception) {
                            Log.e("DetailLombaScreen", "Error mapping document to Lomba object", e)
                            errorMessage = "Gagal memproses data detail lomba."
                        }
                    } else {
                        Log.w("DetailLombaScreen", "Document not found for ID: $lombaId")
                        errorMessage = "Data lomba tidak ditemukan."
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    Log.e("DetailLombaScreen", "Error getting document details: ", exception)
                    errorMessage = "Gagal mengambil detail: ${exception.message}"
                    isLoading = false
                }
        } else {
            Log.e("DetailLombaScreen", "Received empty Lomba ID.")
            errorMessage = "ID Lomba tidak valid."
            isLoading = false // Langsung set loading false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = lombaDetail?.namaLomba ?: "Detail Lomba", // Tampilkan nama jika ada
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) { // Tombol kembali
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B1533)
                )
            )
        },
        containerColor = Color(0xFF1B1533) // Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding dari Scaffold
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Terjadi kesalahan tidak diketahui.",
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                lombaDetail != null -> {
                    // Gunakan data yang sudah di-fetch
                    val lomba = lombaDetail!!
                    // Buat konten bisa di-scroll jika panjang
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()) // Agar bisa scroll
                            .padding(16.dp), // Padding untuk konten detail
                        verticalArrangement = Arrangement.spacedBy(12.dp)

                    ) {
                        // ===== 2. TAMPILKAN GAMBAR DI SINI =====
                        if (!lomba.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = lomba.imageUrl, // URL gambar dari model Lomba
                                contentDescription = "Poster Lomba ${lomba.namaLomba}",
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .height(215.dp)
                                    .width(164.dp)// Sesuaikan tinggi gambar sesuai kebutuhan
                                    .clip(RoundedCornerShape(12.dp)) // Bentuk sudut gambar
                                    .background(Color.Gray.copy(alpha = 0.3f)), // Warna placeholder jika gambar lambat load
                                contentScale = ContentScale.Crop // Atau ContentScale.Fit, dll.
                            )
                            Spacer(modifier = Modifier.height(16.dp)) // Jarak setelah gambar
                        }
                        DetailItem(label = "Nama Lomba", value = lomba.namaLomba)
                        DetailItem(label = "Penyelenggara", value = lomba.penyelenggara)
                        DetailItem(
                            label = "Deskripsi",
                            value = lomba.deskripsi,
                            isLongText = true
                        ) // Tandai jika teks bisa panjang
                        DetailItem(label = "Tanggal Pendaftaran", value = lomba.pendaftaran)
                        DetailItem(label = "Tanggal Pelaksanaan", value = lomba.pelaksanaan)

                        // Tombol Link Pendaftaran
                        lomba.pendaftaran?.takeIf { it.isNotBlank() }?.let { link ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val rawUrl = link.trim()
                                    var processedUrl = rawUrl
                                    try {
                                        if (!processedUrl.startsWith("http://") && !processedUrl.startsWith(
                                                "https://"
                                            )
                                        ) {
                                            processedUrl = "https://$processedUrl"
                                        }
                                        uriHandler.openUri(processedUrl)
                                    } catch (e: Exception) {
                                        Log.e(
                                            "DetailLombaScreen",
                                            "Could not open Uri. Attempted: '$processedUrl'. Error: ${e.message}",
                                            e
                                        )
                                        // Tampilkan feedback ke user jika perlu (misal Toast)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF584ED9
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth() // Buat tombol lebih lebar
                            ) {
                                Text("Buka Link Pendaftaran", color = Color.White)
                            }
                        } ?: run {
                            DetailItem(label = "Link Pendaftaran", value = "Tidak tersedia")
                        }

                        // Tambahkan detail lain jika ada di model Lomba Anda
                        // DetailItem(label = "Kategori", value = lomba.kategori)
                        // DetailItem(label = "Hadiah", value = lomba.hadiah)
                    }
                }
                // Kasus fallback jika lombaDetail null tapi tidak loading/error
                else -> {
                    Text(
                        text = "Detail lomba tidak dapat ditampilkan.",
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

// Composable helper untuk menampilkan label dan value secara konsisten
@Composable
fun DetailItem(label: String, value: String?, isLongText: Boolean = false) {
    Column {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value ?: "N/A", // Tampilkan N/A jika null
            fontSize = 16.sp,
            color = Color.White,
            // Jangan batasi maxLines jika isLongText true
            maxLines = if (isLongText) Int.MAX_VALUE else 10,
            overflow = if (isLongText) TextOverflow.Visible else TextOverflow.Ellipsis
        )
    }
}