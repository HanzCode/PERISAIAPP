package com.example.perisaiapps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.compose.rememberNavController
import com.example.perisaiapps.Navigation.AppNavigation
import com.example.perisaiapps.Screen.HomeScreen
import com.example.perisaiapps.Screen.LoginScreen
import com.example.perisaiapps.ui.theme.PerisaiAppsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            AppNavigation()
//            val loginViewModel = LoginViewModel()
//            PerisaiAppsTheme {
//                LoginScreen(navController, loginViewModel)
//            }
        }
    }
}



