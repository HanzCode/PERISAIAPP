package com.example.perisaiapps.Screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.perisaiapps.Component.BottomBar
import com.example.perisaiapps.ui.screen.UserChatListScreen
import com.example.perisaiapps.viewmodel.UserChatListViewModel

@Composable
// 1. Terima NavController utama dari AppNavigation
fun MainScreen(mainNavController: NavController) {
    // NavController ini khusus untuk mengontrol NavHost di dalam MainScreen (Bottom Bar)
    val bottomNavController: NavHostController = rememberNavController()

    val userChatListViewModel: UserChatListViewModel = viewModel()
    val totalUnreadCount by userChatListViewModel.totalUnreadCount.collectAsState()

    Scaffold(
        // BottomBar tetap menggunakan bottomNavController
        bottomBar = { BottomBar(navController = bottomNavController, totalUnreadCount = totalUnreadCount) }
    ) { innerPadding ->
        // NavHost ini dikontrol oleh bottomNavController
        NavHost(
            navController = bottomNavController,
            startDestination = "home", // Layar awal untuk bottom navigation
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(navController = bottomNavController)
            }
            composable("lomba") {
                InfoLombaScreen(navController = mainNavController)
            }
            composable(
                route = "mentor_list_route?initialQuery={initialQuery}",
                arguments = listOf(navArgument("initialQuery") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val query = backStackEntry.arguments?.getString("initialQuery")
                MentorListScreen(navController = mainNavController, initialQuery = query)
            }
            composable("user_chat_list") {
                UserChatListScreen(navController = mainNavController,  viewModel = userChatListViewModel)
            }
            composable("profile") {
                ProfileScreen(navController = mainNavController)
            }
        }
    }
}
