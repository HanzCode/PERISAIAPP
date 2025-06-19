package com.example.perisaiapps.Screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield // Contoh Ikon, ganti dengan logo Anda
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

// Warna background sesuai tema admin Anda
private val darkBackground = Color(0xFF120E26)
private val accentColor = Color(0xFF8A2BE2)

@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 3000), // Durasi animasi fade-in
        label = "Splash Alpha Animation"
    )

    // Efek ini berjalan satu kali saat layar pertama kali muncul
    LaunchedEffect(key1 = true) {
        startAnimation = true // Mulai animasi
        delay(4000) // Tampilkan splash screen selama 4 detik

        // Setelah delay, hapus splash screen dari back stack dan navigasi ke login
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Shield, // TODO: Ganti dengan logo Anda
                contentDescription = "App Logo",
                tint = accentColor,
                modifier = Modifier
                    .size(120.dp)
                    .alpha(alphaAnim.value) // Terapkan animasi alpha
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Perisai",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
    }
}