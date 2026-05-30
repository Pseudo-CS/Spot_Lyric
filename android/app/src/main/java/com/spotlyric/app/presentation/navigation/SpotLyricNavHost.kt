package com.spotlyric.app.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spotlyric.app.presentation.auth.AuthScreen
import com.spotlyric.app.presentation.lyrics.LyricsScreen
import com.spotlyric.app.presentation.manage.ManageScreen
import com.spotlyric.app.presentation.player.PlayerScreen
import com.spotlyric.app.presentation.settings.SettingsScreen
import com.spotlyric.app.presentation.sources.SourcesScreen
import com.spotlyric.app.presentation.theme.DarkSurface

@Composable
fun SpotLyricNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Bottom bar is visible on Player, Lyrics, Manage, Sources, and Settings screens (not Auth)
    val showBottomBar = currentDestination?.route?.let { route ->
        route != Screen.Auth.route && route != Screen.Lyrics.route
    } ?: false

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Auth.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthenticated = {
                        navController.navigate(Screen.Player.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(Screen.Player.route) {
                PlayerScreen(
                    onNavigateToLyrics = { songName, artistName ->
                        navController.navigate(Screen.Lyrics.createRoute(songName, artistName))
                    },
                )
            }

            composable(
                route = Screen.Lyrics.route,
                arguments = listOf(
                    navArgument("songName") { type = NavType.StringType },
                    navArgument("artistName") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val songName = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("songName") ?: "",
                    "UTF-8",
                )
                val artistName = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("artistName") ?: "",
                    "UTF-8",
                )
                LyricsScreen(
                    songName = songName,
                    artistName = artistName,
                    onBack = {
                        navController.navigate(Screen.Manage.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable(Screen.Manage.route) {
                ManageScreen(
                    onNavigateToLyrics = { songName, artistName ->
                        navController.navigate(Screen.Lyrics.createRoute(songName, artistName))
                    },
                )
            }

            composable(Screen.Sources.route) {
                SourcesScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

