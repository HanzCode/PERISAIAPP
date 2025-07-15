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
import androidx.navigation.NavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Data class sederhana untuk item peminatan
data class PeminatanItem(val title: String, val icon: ImageVector)

@Composable
fun PeminatanSection(navController: NavController, modifier: Modifier = Modifier) {
    val peminatanItems = listOf(
        PeminatanItem("POSTER", Icons.Default.DesignServices),
        PeminatanItem("VIDEO", Icons.Default.Code),
        PeminatanItem("KTI", Icons.Default.QueryStats),
        PeminatanItem("DEBAT", Icons.AutoMirrored.Filled.MenuBook)
    )

    Column(modifier = modifier) {
        Text(
            text = "Peminatan Populer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // 2. Teruskan aksi klik ke setiap kartu
                peminatanItems.take(2).forEach { item ->
                    PeminatanCard(
                        item = item,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            // 3. Lakukan navigasi dengan argumen
                            val encodedQuery = URLEncoder.encode(item.title, StandardCharsets.UTF_8.name())
                            navController.navigate("mentor_list_route?initialQuery=$encodedQuery")
                        }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                peminatanItems.drop(2).forEach { item ->
                    PeminatanCard(
                        item = item,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val encodedQuery = URLEncoder.encode(item.title, StandardCharsets.UTF_8.name())
                            navController.navigate("mentor_list_route?initialQuery=$encodedQuery")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PeminatanCard(item: PeminatanItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .aspectRatio(1.5f) // Persegi panjang
            .clickable(onClick = onClick),
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
                tint = Color(0xFFFFC107),
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