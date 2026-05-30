package com.spotlyric.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {

    data object Auth : Screen("auth")

    data object Player : Screen("player")

    data object Lyrics : Screen("lyrics/{songName}/{artistName}") {
        fun createRoute(songName: String, artistName: String): String {
            return "lyrics/${java.net.URLEncoder.encode(songName, "UTF-8")}/${java.net.URLEncoder.encode(artistName, "UTF-8")}"
        }
    }

    data object Manage : Screen("manage")

    data object Sources : Screen("sources")

    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Player, "Player", Icons.Default.PlayCircle),
    BottomNavItem(Screen.Sources, "Sources", Icons.Default.Star),
    BottomNavItem(Screen.Manage, "Library", Icons.Default.LibraryMusic),
    BottomNavItem(Screen.Settings, "Settings", Icons.Default.Settings),
)
