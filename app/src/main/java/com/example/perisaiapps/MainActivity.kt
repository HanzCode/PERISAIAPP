package com.example.perisaiapps

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permission", "Izin notifikasi diberikan.")
        } else {
            Log.w("Permission", "Izin notifikasi ditolak.")
        }
    }
    private fun askNotificationPermission() {
        // Hanya minta izin di Android 13 (TIRAMISU) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatIdFromNotification = intent.getStringExtra("chatId")
        askNotificationPermission()
        setContent {
            PerisaiAppsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(startChatId = chatIdFromNotification)
                }
            }
        }
    }

}





