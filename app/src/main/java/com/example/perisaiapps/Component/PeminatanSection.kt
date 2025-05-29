package com.example.perisaiapps.Component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth // Import untuk fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PeminatanSection() {
    Column { // Column ini akan menyesuaikan tingginya dengan konten di dalamnya
        Text(
            text = "Peminatan",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // Spacer(modifier = Modifier.height(18.dp)) // Bisa dihapus jika padding bottom pada Text sudah cukup

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            // ===== PERBAIKAN DI SINI =====
            // Ganti fillMaxHeight() dengan tinggi yang spesifik.
            // Misal, jika Anda punya 4 item dan 2 kolom, itu 2 baris.
            // Jika tiap item punya aspectRatio(1f) dan lebar (screenWidth/2 - Spacing),
            // Anda bisa kalkulasi atau tentukan tinggi tetap yang cukup untuk 2 baris.
            // Contoh: kita set tinggi yang cukup untuk sekitar 2 baris item persegi.
            // Jika satu item kira-kira 150.dp x 150.dp, maka 2 baris + spacing = sekitar 310-320.dp
            modifier = Modifier
                .height(320.dp) // Tentukan tinggi yang pasti, misal 320.dp
                .fillMaxWidth(), // Biarkan grid mengisi lebar
            // =============================
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            // userScrollEnabled = true // LazyVerticalGrid akan bisa di-scroll jika kontennya melebihi 320.dp ini.
            // Jika Anda TIDAK ingin LazyVerticalGrid ini bisa di-scroll sama sekali
            // (karena HomeScreen sudah scroll), dan itemnya sedikit,
            // pertimbangkan untuk tidak menggunakan LazyVerticalGrid, tapi Column/Row biasa.
            // Namun, dengan items(4), tinggi 320.dp mungkin tidak akan membuatnya scroll.
        ) {
            items(4) { // Dengan items(4) dan 2 kolom, ini hanya 2 baris.
                Box(
                    modifier = Modifier
                        .aspectRatio(1f) // Membuat item menjadi persegi
                        .background(Color.LightGray, shape = RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1533) // Tambahkan background untuk preview
@Composable
fun PeminatanSectionPreview() {
    // Untuk preview yang lebih baik, Anda bisa membungkusnya dengan padding atau background
    Box(modifier = Modifier.background(Color(0xFF1B1533)).padding(16.dp)) {
        PeminatanSection()
    }
}