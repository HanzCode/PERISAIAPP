package com.example.perisaiapps.Component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.perisaiapps.Item.BottomNavItem

@Composable
fun BottomBar(navController: NavController){
    val items = listOf(
        BottomNavItem(
            title = "Home",
            icon = Icons.Default.Home,
            route = "home"
        ),BottomNavItem(
            title = "Profile",
            icon = Icons.Default.Home,
            route = "profile"
        ),
        BottomNavItem(
            title = "Settings",
            icon = Icons.Default.Home,
            route = "settings"
        )
    )
    NavigationBar(
        containerColor = Color(0xFF4B3D78),
        tonalElevation = 0.dp
    ) {
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination

        items.forEach { item ->
            val selected = currentDestination?.route == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selected) Color.Cyan else Color.White
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (selected) Color.Cyan else Color.White
                    )
                },
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }

}