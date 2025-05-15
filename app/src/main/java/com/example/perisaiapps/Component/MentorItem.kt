package com.example.perisaiapps.Component

import android.util.Log // Import Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // <-- 1. IMPORT clickable
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
// import androidx.compose.ui.tooling.preview.Preview // Preview bisa di-disable jika butuh NavController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.perisaiapps.Model.Mentor
import java.util.Date
import coil.compose.AsyncImage

@Composable
fun MentorItem(
    mentor: Mentor,
    // <-- 2. TAMBAHKAN PARAMETER onItemClick -->
    onItemClick: (String) -> Unit
) {
    val isReady = mentor.availableUntil?.toDate()?.after(Date()) == true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // Clip bentuk di Row agar clickable mengikuti bentuk
            // <-- 3. TAMBAHKAN MODIFIER CLICKABLE -->
            .clickable(
                // Aktifkan klik hanya jika ID mentor ada dan tidak kosong
                enabled = !mentor.id.isBlank(), // ASUMSI: mentor.id ada di model Anda
                onClick = {
                    // Panggil lambda onItemClick dengan ID mentor
                    Log.d("MentorItem", "MentorItem clicked, ID: ${mentor.id}")
                    onItemClick(mentor.id)
                }
            )
            .background(Color(0xFF2A2342)) // Tambahkan background jika Card dihilangkan
            .padding(12.dp), // Padding yang sebelumnya di Card
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = mentor.photoUrl,
            contentDescription = "Foto Mentor ${mentor.name}",
            modifier = Modifier
                .height(159.dp) // Mungkin terlalu besar, sesuaikan jika perlu jadi misal 80.dp
                .width(139.dp)  // Mungkin terlalu besar, sesuaikan jika perlu jadi misal 80.dp
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(mentor.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text(mentor.peminatan, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }

        Box(
            modifier = Modifier
                .background(
                    color = if (isReady) Color(0xFF00C853) else Color(0xFFD50000),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isReady) "Ready" else "Not Ready",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}