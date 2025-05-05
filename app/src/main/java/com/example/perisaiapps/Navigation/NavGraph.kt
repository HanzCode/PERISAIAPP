package com.example.perisaiapps.Navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.perisaiapps.Screen.HomeScreen
import com.example.perisaiapps.Screen.InfoLombaScreen
import com.example.perisaiapps.Screen.LoginScreen
import com.example.perisaiapps.Screen.MainScreen
import com.example.perisaiapps.Screen.MentorListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Tentukan layar awal di sini, mungkin perlu cek status login awal Firebase
    val startDestination = "home" // Atau "home" jika sudah login

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            // Teruskan navController ke LoginScreen
            LoginScreen(navController = navController)
        }
        composable("home") {
            MainScreen()
        }
        composable("Lomba") {
            InfoLombaScreen()
        }
        composable("Mentor") {
            MentorListScreen()
        }

    }
}

