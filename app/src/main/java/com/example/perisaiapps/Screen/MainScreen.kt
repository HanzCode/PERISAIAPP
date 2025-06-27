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
                HomeScreen(navController = mainNavController)
            }
            composable("lomba") {
                InfoLombaScreen(navController = mainNavController)
            }
            composable("mentor") {
                MentorListScreen(navController = mainNavController)
            }
            composable("profile") {
                ProfileScreen(navController = mainNavController)
            }
        }
    }
}
