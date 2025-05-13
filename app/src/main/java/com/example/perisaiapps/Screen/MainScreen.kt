package com.example.perisaiapps.Screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.perisaiapps.Component.BottomBar

@Composable
// 1. Terima NavController utama dari AppNavigation
fun MainScreen(mainNavController: NavController) {
    // NavController ini khusus untuk mengontrol NavHost di dalam MainScreen (Bottom Bar)
    val bottomNavController: NavHostController = rememberNavController()

    Scaffold(
        // BottomBar tetap menggunakan bottomNavController
        bottomBar = { BottomBar(navController = bottomNavController) }
    ) { innerPadding ->
        // NavHost ini dikontrol oleh bottomNavController
        NavHost(
            navController = bottomNavController,
            startDestination = "home", // Layar awal untuk bottom navigation
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                // 3. Perbaiki panggilan HomeScreen.
                // Tentukan NavController mana yang dibutuhkan HomeScreen:
                // - Jika perlu navigasi ke detail (di luar MainScreen) -> mainNavController
                // - Jika perlu pindah tab bottom nav -> bottomNavController
                // - Jika tidak perlu navigasi -> tidak perlu parameter NavController
                // Contoh: Meneruskan bottomNavController jika perlu pindah tab dari dalam HomeScreen
                HomeScreen(navController = bottomNavController)
            }
            composable("lomba") {
                // 2. InfoLombaScreen butuh mainNavController untuk ke DetailLombaScreen
                InfoLombaScreen(navController = mainNavController)
            }
//            composable("mentor") {
//                // 2. MentorListScreen mungkin butuh mainNavController untuk ke detail mentor
//                // Sesuaikan jika MentorListScreen tidak butuh navigasi keluar
//                MentorListScreen(navController = mainNavController) // Asumsi butuh mainNavController
//            }
            // Tambahkan tujuan bottom navigation lain di sini jika perlu
        }
    }
}
