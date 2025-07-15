package com.example.perisaiapps.Component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Lomba

// Warna yang konsisten
private val cardBackground = Color(0xFF1F1A38)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val accentColor = Color(0xFF8A2BE2)

@Composable
fun LombaListItem(
    lomba: Lomba,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(lomba.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Kolom untuk Poster Lomba
            AsyncImage(
                model = lomba.imageUrl,
                contentDescription = "Poster ${lomba.namaLomba}",
                modifier = Modifier
                    .width(100.dp)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Kolom untuk Detail Teks
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nama Lomba
                Text(
                    text = lomba.namaLomba,
                    fontWeight = FontWeight.Bold,
                    color = textColorPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Penyelenggara
                InfoRow(
                    icon = Icons.Default.CorporateFare,
                    text = lomba.penyelenggara
                )
                // Info Pendaftaran (Deadline)
                InfoRow(
                    icon = Icons.Default.CalendarMonth,
                    text = "Deadline: ${lomba.pendaftaran}"
                )
            }
        }
    }
}
// Composable helper kecil untuk baris info dengan ikon
@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColorSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = textColorSecondary,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}