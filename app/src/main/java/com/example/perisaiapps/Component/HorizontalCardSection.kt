package com.example.perisaiapps.Component // Sesuaikan package

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.perisaiapps.Model.Lomba // Pastikan path model benar
import java.util.UUID // Untuk fallback key

@Composable
fun HorizontalCardSection(
    title: String,
    lombaList: List<Lomba>,
    navController: NavController,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp), // Sesuaikan dengan tinggi LombaCardItem
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorMessage, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                }
            }
            lombaList.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Belum ada lomba untuk ditampilkan.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
            else -> {
                LazyRow(
                    contentPadding = PaddingValues(vertical = 8.dp), // Padding atas bawah untuk row
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Jarak antar kartu
                ) {
                    items(lombaList, key = { lomba -> lomba.id.takeIf { !it.isBlank() } ?: UUID.randomUUID().toString() }) { lomba ->
                        LombaCardItem(lomba = lomba, navController = navController)
                    }
                }
            }
        }
    }
}