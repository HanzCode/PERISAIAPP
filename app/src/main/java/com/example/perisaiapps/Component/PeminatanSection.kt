package com.example.perisaiapps.Component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Data class sederhana untuk item peminatan
data class PeminatanItem(val title: String, val icon: ImageVector)

@Composable
fun PeminatanSection(modifier: Modifier = Modifier) {
    val peminatanItems = listOf(
        PeminatanItem("UI/UX Design", Icons.Default.DesignServices),
        PeminatanItem("Web Dev", Icons.Default.Code),
        PeminatanItem("Data Science", Icons.Default.QueryStats),
        PeminatanItem("Karya Tulis", Icons.AutoMirrored.Filled.MenuBook)
    )

    Column(modifier = modifier) {
        Text(
            text = "Peminatan Populer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))
        // Menggunakan 2 Row untuk membuat grid 2x2 tanpa nested scrolling
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PeminatanCard(item = peminatanItems[0], modifier = Modifier.weight(1f))
                PeminatanCard(item = peminatanItems[1], modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PeminatanCard(item = peminatanItems[2], modifier = Modifier.weight(1f))
                PeminatanCard(item = peminatanItems[3], modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PeminatanCard(item: PeminatanItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1.5f) // Persegi panjang
            .clickable { /* TODO: Navigasi ke halaman peminatan */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1A38))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = Color(0xFF8A2BE2),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}