package com.example.perisaiapps.Component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.perisaiapps.Model.Mentor
import java.util.Date
import coil.compose.AsyncImage // Import ini penting!
@Composable
fun MentorItem(mentor: Mentor) {
    val isReady = mentor.availableUntil?.toDate()?.after(Date()) == true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = mentor.photoUrl,
            contentDescription = "Foto Mentor ${mentor.name}", // Deskripsi lebih baik
            modifier = Modifier
                .height(159.dp)
                .width(139.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop // Agar gambar terpotong rapi jika rasio beda
            // Anda bisa tambahkan placeholder atau error state di sini jika mau
            // placeholder = painterResource(id = R.drawable.placeholder_image),
            // error = painterResource(id = R.drawable.error_image)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(mentor.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp) // Sedikit besarkan font
            Text(mentor.peminatan, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }

        Box(
            modifier = Modifier
                .background(
                    color = if (isReady) Color(0xFF00C853) else Color(0xFFD50000), // Gunakan 'color ='
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isReady) "Ready" else "Not Ready",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium // Sedikit tebalkan
            )
        }
    }
}


