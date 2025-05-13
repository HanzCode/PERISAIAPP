package com.example.perisaiapps.Component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Pastikan import clickable
import androidx.compose.foundation.layout.* // Import semua dari layout jika perlu
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Import TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler // Import UriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Lomba // Pastikan model Lomba diimport

@Composable
fun LombaItem(
    lomba: Lomba,
    // Lambda ini HANYA untuk navigasi ke detail menggunakan ID Lomba
    onItemClick: (String) -> Unit
) {
    // Ambil UriHandler jika akan digunakan untuk tombol link
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // == Klik pada Card sekarang memicu Navigasi menggunakan ID ==
            .clickable(
                // Aktifkan klik hanya jika ID lomba ada dan tidak kosong
                enabled = !lomba.id.isNullOrBlank(),
                onClick = {
                    // Panggil onItemClick dengan ID Lomba yang valid
                    lomba.id?.let { id ->
                        Log.d("LombaItem", "Card clicked -> Navigating with ID: $id")
                        onItemClick(id) // Panggil lambda navigasi dengan ID
                    } ?: run {
                        // Safety log jika ID null saat diklik (seharusnya tidak terjadi)
                        Log.w("LombaItem", "Card clicked but lomba ID is null/blank for ${lomba.namaLomba}")
                    }
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2342))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // AsyncImage (Gambar Lomba) - Tidak perlu diubah
            AsyncImage(
                model = lomba.imageUrl, // Pastikan field ini ada di model Lomba Anda
                contentDescription = "Poster ${lomba.namaLomba}",
                modifier = Modifier
                    .width(100.dp)
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Teks Info Lomba (Nama, Penyelenggara, Deskripsi, Tanggal) - Tidak perlu diubah
                Text(lomba.namaLomba, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (lomba.penyelenggara.isNotBlank()) {
                    Text("Oleh: ${lomba.penyelenggara}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(lomba.deskripsi, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                if (lomba.pendaftaran.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(lomba.pendaftaran, color = Color(0xFF82D8FF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                if (lomba.pelaksanaan.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(lomba.pelaksanaan, color = Color(0xFF82D8FF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                // == Penanganan Terpisah untuk LinkInfo ==
                // Cek jika LinkInfo ada dan tidak kosong
                if (lomba.LinkInfo.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp)) // Beri jarak

                    // Gunakan TextButton agar jelas ini bisa diklik & terpisah dari klik Card
                    TextButton(
                        onClick = {
                            // Aksi klik ini HANYA membuka URL dari LinkInfo
                            val url = lomba.LinkInfo.trim()
                            Log.d("LombaItem", "Link button clicked -> Opening URL: $url")
                            try {
                                var processedUrl = url
                                // Tambahkan skema jika belum ada
                                if (!processedUrl.startsWith("http://") && !processedUrl.startsWith("https://")) {
                                    processedUrl = "https://$processedUrl"
                                }
                                uriHandler.openUri(processedUrl)
                            } catch (e: Exception) {
                                Log.e("LombaItem", "Could not open Uri: $url", e)
                                // Tampilkan pesan ke user jika perlu (misal: Toast)
                            }
                        },
                        modifier = Modifier.align(Alignment.End) // Taruh di ujung kanan
                        // Atur padding jika perlu agar tidak terlalu mepet
                        // .padding(top = 4.dp, end = 4.dp)
                    ) {
                        // Teks di dalam tombol link
                        Text(
                            text = "Info Selengkapnya...",
                            color = Color.Cyan, // Warna link Anda
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                // == Akhir Penanganan LinkInfo ==
            }
        }
    }
}