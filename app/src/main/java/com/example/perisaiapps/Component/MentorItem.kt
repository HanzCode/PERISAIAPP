package com.example.perisaiapps.Component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Mentor

@Composable
fun MentorItem(
    mentor: Mentor,
    onItemClick: (String) -> Unit
) {
    val isReady = mentor.bersediaKah

    // Gunakan Card untuk elevasi dan bentuk yang lebih baik
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = mentor.id.isNotBlank(),
                onClick = { onItemClick(mentor.id) }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // Menggunakan warna kartu dari tema
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = mentor.photoUrl,
                contentDescription = "Foto Mentor ${mentor.name}",
                modifier = Modifier
                    .height(120.dp) // Sesuaikan ukuran agar lebih proporsional
                    .width(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiary), // Warna placeholder
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mentor.name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface, // Warna teks utama
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mentor.peminatan,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Warna teks sekunder
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Status Ketersediaan
            Box(
                modifier = Modifier
                    .background(
                        // Tetap gunakan warna spesifik untuk status, tapi bisa juga didefinisikan di tema
                        color = if (isReady) Color(0xFF00C853) else Color(0xFFD50000),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isReady) "Bersedia" else "Sibuk",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}