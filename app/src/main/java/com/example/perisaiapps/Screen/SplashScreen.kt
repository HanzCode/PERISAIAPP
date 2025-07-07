package com.example.perisaiapps.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.perisaiapps.R
import com.example.perisaiapps.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = viewModel()
) {
    val nextRoute by viewModel.nextRoute.collectAsState()

    // Efek ini akan berjalan saat 'nextRoute' mendapatkan nilai dari ViewModel
    LaunchedEffect(key1 = nextRoute) {
        // Beri sedikit jeda agar splash screen terlihat
        delay(1500)

        nextRoute?.let { route ->
            navController.navigate(route) {
                // Hapus splash screen dari back stack
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Tampilkan logo Anda di sini
        Image(
            painter = painterResource(id = R.drawable.logo_perisai), // Sesuaikan dengan logo Anda
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp)
        )
    }
}