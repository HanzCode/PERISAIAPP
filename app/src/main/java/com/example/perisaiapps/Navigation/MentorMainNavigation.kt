package com.example.perisaiapps.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.perisaiapps.ui.screen.mentor.ChatListScreen
import com.example.perisaiapps.ui.screen.mentor.MentorProfileScreen
import com.example.perisaiapps.viewmodel.MentorChatListViewModel

sealed class MentorScreen(val route: String, val title: String, val icon: ImageVector) {
    object Chat : MentorScreen("mentor_chat", "Chat", Icons.Default.Chat)
    object Profile : MentorScreen("mentor_profile", "Profil", Icons.Default.Person)
}

val mentorBottomNavItems = listOf(
    MentorScreen.Chat,
    MentorScreen.Profile
)

@Composable
fun MentorMainNavigation(
    // Terima NavController utama dari AppNavigation
    rootNavController: NavController
) {
    // NavController ini hanya untuk mengurus perpindahan antar tab di bawah
    val nestedNavController = rememberNavController()

    val chatListViewModel: MentorChatListViewModel = viewModel()
    val totalUnreadCount by chatListViewModel.totalUnreadCount.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                mentorBottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            nestedNavController.navigate(screen.route) {
                                popUpTo(nestedNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            // 3. Logika untuk menampilkan badge notifikasi
                            if (screen.route == MentorScreen.Chat.route) {
                                BadgedBox(
                                    badge = {
                                        if (totalUnreadCount > 0) {
                                            // Badge akan muncul jika ada pesan belum dibaca
                                            Badge { Text(totalUnreadCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            } else {
                                // Untuk ikon lain, tampilkan seperti biasa
                                Icon(screen.icon, contentDescription = screen.title)
                            }
                        },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.background
                        )
                    )

                }
            }
        }
    ) { innerPadding ->
        // NavHost ini HANYA berisi layar-layar yang memiliki Bottom Bar
        NavHost(
            navController = nestedNavController,
            startDestination = MentorScreen.Chat.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(MentorScreen.Chat.route) {
                // Berikan rootNavController agar bisa navigasi ke detail_chat
                ChatListScreen(navController = rootNavController, viewModel = chatListViewModel)
            }
            composable(MentorScreen.Profile.route) {
                MentorProfileScreen(
                    // Berikan rootNavController agar bisa navigasi ke edit_mentor_profile
                    onNavigateToEdit = { mentorId ->
                        rootNavController.navigate("edit_mentor_profile/$mentorId")
                    },
                    onNavigateToChangePassword = {
                        rootNavController.navigate("change_password")
                    }

                )
            }
        }
    }
}


