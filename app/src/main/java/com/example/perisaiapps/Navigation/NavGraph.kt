package com.example.perisaiapps.Navigation

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.perisaiapps.Model.Mentor
import com.example.perisaiapps.Screen.AdminScreen.AddEditLombaScreen
import com.example.perisaiapps.Screen.AdminScreen.AddUserScreen
import com.example.perisaiapps.Screen.AdminScreen.AdminDashboardScreen
import com.example.perisaiapps.Screen.AdminScreen.AdminManageLombaScreen
import com.example.perisaiapps.Screen.AdminScreen.AdminManageMentorsScreen
import com.example.perisaiapps.Screen.AdminScreen.AdminManageUsersScreen
import com.example.perisaiapps.Screen.DetailLombaScreen
import com.example.perisaiapps.Screen.DetailMentorScreen
import com.example.perisaiapps.Screen.InfoLombaScreen
import com.example.perisaiapps.Screen.LoginScreen
import com.example.perisaiapps.Screen.MainScreen
import com.example.perisaiapps.Screen.MentorListScreen
import com.example.perisaiapps.Screen.SplashScreen
import com.example.perisaiapps.Screen.admin.AddEditMentorScreen
import com.example.perisaiapps.ui.screen.mentor.DetailChatScreen
import com.example.perisaiapps.ui.screen.mentor.NotesScreen
import com.example.perisaiapps.ui.theme.PerisaiAppsTheme
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("ComposableDestinationInComposeScope")
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Tentukan layar awal di sini, mungkin perlu cek status login awal Firebase
    val startDestination = "splash" // Atau "home" jika sudah login
    val auth = FirebaseAuth.getInstance()
    LaunchedEffect(key1 = auth) {
        auth.addAuthStateListener { firebaseAuth ->
            // Jika tidak ada user yang login (misal setelah logout),
            // paksa navigasi kembali ke halaman login.
            if (firebaseAuth.currentUser == null) {
                navController.navigate("login") {
                    // Hapus semua halaman dari backstack agar user tidak bisa kembali
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("home") {
            MainScreen(mainNavController = navController)
        }
        composable("admin_dashboard_route") {
            AdminDashboardScreen(navController = navController)
        }

        composable("Lomba") {
            InfoLombaScreen(navController = navController)
        }
        composable("add_user_route") {
            AddUserScreen(navController = navController)
        }

        composable("Mentor") {
            MentorListScreen(navController = navController)
        }
        composable(
            route = "detail_chat/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            PerisaiAppsTheme {
                DetailChatScreen(
                    chatId = chatId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToNotes = {
                        navController.navigate("notes/$chatId")
                    }
                )
            }
        }
        composable(
            route = "notes/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            NotesScreen(
                chatId = chatId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "detail_lomba/{lombaId}", // Definisikan rute dengan argumen {lombaId}
            arguments = listOf(navArgument("lombaId") { // Definisikan argumen lombaId
                type = NavType.StringType // Tipe argumen adalah String
            })
        ) { backStackEntry ->
            // Ambil argumen 'lombaId' dari backStackEntry
            val lombaId = backStackEntry.arguments?.getString("lombaId")

            // Validasi bahwa lombaId tidak null atau kosong sebelum meneruskannya
            if (!lombaId.isNullOrEmpty()) {
                DetailLombaScreen(navController = navController, lombaId = lombaId)
            } else {
                // Jika ID tidak valid (misal null atau string kosong)
                // Tampilkan pesan error atau navigasi kembali
                Log.e("AppNavigation", "Error: Lomba ID is null or empty in detail_lomba route.")
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Terjadi Kesalahan: ID Lomba tidak valid.", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) { // Tombol untuk kembali
                        Text("Kembali")
                    }
                }

            }

        }
        composable(
            route = "detail_mentor/{mentorId}",
            arguments = listOf(navArgument("mentorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mentorId = backStackEntry.arguments?.getString("mentorId")
            if (!mentorId.isNullOrEmpty()) {
                DetailMentorScreen(navController = navController, mentorId = mentorId)
            } else {
                // Handle ID mentor tidak valid
                Log.e("AppNavigation", "Error: Mentor ID is null or empty in detail_mentor route.")
                // Tampilkan pesan error atau navigasi kembali
                Text("Error: ID Mentor tidak valid.")
            }
        }
        // Rute Admin
        composable(
            route = "add_edit_mentor?mentorId={mentorId}", // mentorId opsional
            arguments = listOf(navArgument("mentorId") { nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val mentorId = backStackEntry.arguments?.getString("mentorId")
            AddEditMentorScreen(navController = navController, mentorId = mentorId)
        }
        composable("admin_manage_mentors_route") {
            AdminManageMentorsScreen(navController = navController)
        }
        composable("admin_manage_users_route") {
            AdminManageUsersScreen(navController = navController)
        }
        composable("admin_manage_lomba_route") {
            AdminManageLombaScreen(navController = navController)
        }
        composable(
            route = "add_edit_lomba?lombaId={lombaId}",
            arguments = listOf(navArgument("lombaId") { nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val lombaId = backStackEntry.arguments?.getString("lombaId")
            AddEditLombaScreen(navController = navController, lombaId = lombaId)
        }
        // rute Mentor
        composable("mentor_main_route") {
            // Kita bungkus dengan tema gelap agar semua layar di dalamnya menggunakan palet baru
            PerisaiAppsTheme {
                MentorMainNavigation()
            }

        }
    }
}


