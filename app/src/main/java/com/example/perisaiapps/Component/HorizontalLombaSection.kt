package com.example.perisaiapps.Component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Lomba

private val placeholderColor = Color(0xFFE0E0E0).copy(alpha = 0.5f)
private val cardBackgroundColor = Color(0xFF1F1A38)
@Composable
fun HorizontalLombaSection(
    lombaList: List<Lomba>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp) // Sesuaikan padding
    ) {
        items(lombaList, key = { it.id }) { lomba ->
            LombaCard(lomba = lomba, onClick = {
                navController.navigate("detail_lomba/${lomba.id}")
            })
        }
    }
}
@Composable
fun LombaCard(
    lomba: Lomba,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(180.dp) // Lebar kartu
            .aspectRatio(3f / 4f) // Rasio kartu agar sedikit portrait seperti poster
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), // Sudut membulat
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        if (lomba.imageUrl.isNotBlank()) {
            // Tampilkan gambar jika URL ada
            AsyncImage(
                model = lomba.imageUrl,
                contentDescription = "Poster Lomba ${lomba.namaLomba}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Crop agar gambar mengisi kartu
            )
        } else {
            // Tampilkan placeholder abu-abu jika URL gambar kosong
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(placeholderColor)
            )
        }
    }
}