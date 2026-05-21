package com.unitn.audioindexer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unitn.audioindexer.ui.screens.albums.FoldersScreen
import com.unitn.audioindexer.ui.screens.albums.HomeScreen
import com.unitn.audioindexer.ui.screens.albums.PlaylistDetailScreen
import com.unitn.audioindexer.ui.screens.albums.Screen
import com.unitn.audioindexer.ui.screens.albums.SongsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        composable(Screen.Songs.route) {
            SongsScreen()
        }

        composable(Screen.Folders.route) {
            FoldersScreen()
        }

        composable(Screen.PlaylistDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("playlistId")
            PlaylistDetailScreen(id)
        }
    }
}