package com.example.perisaiapps.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.perisaiapps.Model.Mentor
import com.example.perisaiapps.ui.screen.mentor.ChatListScreen
import com.example.perisaiapps.ui.screen.mentor.DetailChatScreen
import com.example.perisaiapps.ui.screen.mentor.EditMentorProfileScreen
import com.example.perisaiapps.ui.screen.mentor.MentorProfileScreen
import com.example.perisaiapps.ui.screen.mentor.NotesScreen


sealed class MentorScreen(val route: String, val title: String, val icon: ImageVector) {
//    object Beranda : MentorScreen("mentor_beranda", "Beranda", Icons.Default.Home)
    object Chat : MentorScreen("mentor_chat", "Chat", Icons.Default.Chat)
    object Profile : MentorScreen("mentor_profile", "Profil", Icons.Default.Person)
}


val mentorBottomNavItems = listOf(
    MentorScreen.Chat,
    MentorScreen.Profile
)

@Composable
fun MentorMainNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                mentorBottomNavItems.forEach { screen ->
                    val isSelected = currentRoute?.startsWith(screen.route) ?: false
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
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
        NavHost(
            navController = navController,
            startDestination = MentorScreen.Chat.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(MentorScreen.Chat.route) {
                ChatListScreen(onChatClicked = { chatId ->
                    navController.navigate("detail_chat/$chatId")
                })
            }
            composable(MentorScreen.Profile.route) {
                MentorProfileScreen(
                    onNavigateToEdit = { mentorId ->
                        navController.navigate("edit_mentor_profile/$mentorId")
                    }
                )
            }

            composable(
                route = "edit_mentor_profile/{mentorId}",
                arguments = listOf(navArgument("mentorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val mentorId = backStackEntry.arguments?.getString("mentorId") ?: ""
                EditMentorProfileScreen(
                    mentorId = mentorId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "detail_chat/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                DetailChatScreen(
                    chatId = chatId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToNotes = { navController.navigate("notes/$chatId") }
                )
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
        }
    }
}