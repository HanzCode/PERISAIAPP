package com.example.perisaiapps.Component // Sesuaikan package

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box // Box bisa digunakan sebagai container gambar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
// Text tidak lagi dibutuhkan jika hanya gambar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Lomba // Pastikan path model benar

@Composable
fun LombaCardItem( // Anda bisa menamai ulang ini menjadi LombaImageCardItem jika perlu
    lomba: Lomba,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp) // Lebar kartu untuk gambar, bisa disesuaikan
            .height(200.dp) // Tinggi kartu untuk gambar, bisa disesuaikan (misal, rasio poster)
            .clickable(
                enabled = !lomba.id.isNullOrBlank(), // Pastikan ID ada
                onClick = {
                    if (!lomba.id.isNullOrBlank()) {
                        Log.d("LombaCardItem", "Image Only Card: Navigating to detail_lomba/${lomba.id}")
                        navController.navigate("detail_lomba/${lomba.id}")
                    } else {
                        Log.w("LombaCardItem", "Lomba ID is blank for ${lomba.namaLomba}, cannot navigate.")
                    }
                }
            ),
        shape = RoundedCornerShape(12.dp), // Bentuk kartu
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Sedikit shadow
        // colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Jika ingin gambar tanpa background card eksplisit
    ) {
        // Card sekarang langsung berisi AsyncImage
        AsyncImage(
            model = lomba.imageUrl,
            contentDescription = "Poster Lomba: ${lomba.namaLomba}", // Deskripsi untuk aksesibilitas
            modifier = Modifier
                .fillMaxSize() // Gambar mengisi seluruh area Card
                .background(Color.Gray.copy(alpha = 0.1f)), // Background placeholder lembut
            contentScale = ContentScale.Crop // Crop agar gambar mengisi tanpa distorsi
        )
    }
}