package com.unitn.audioindexer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.screens.albums.AlbumDetailScreen
import com.unitn.audioindexer.ui.screens.albums.AlbumsScreen
import com.unitn.audioindexer.ui.screens.artists.ArtistDetailScreen
import com.unitn.audioindexer.ui.screens.artists.ArtistsScreen
import com.unitn.audioindexer.ui.screens.player.PlayerScreen
import com.unitn.audioindexer.ui.screens.playlists.PlaylistDetailScreen
import com.unitn.audioindexer.ui.screens.playlists.PlaylistsScreen
import com.unitn.audioindexer.ui.screens.tracks.TracksScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Tracks.route,
        modifier = modifier
    ) {
        composable(Screen.Tracks.route) {
            TracksScreen(navController)
        }

        composable(Screen.Albums.route) {
            AlbumsScreen(navController)
        }

        composable(Screen.Artists.route) {
            ArtistsScreen(navController)
        }

        composable(Screen.Playlists.route) {
            PlaylistsScreen(navController)
        }

        composable(Screen.Album.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("albumId")?.toInt()
            AlbumDetailScreen(
                id = id,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(Screen.Artist.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("artistId")?.toInt()
            ArtistDetailScreen(
                id = id,
                navController = navController,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(Screen.Playlist.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("playlistId")?.toInt()
            PlaylistDetailScreen(
                id = id,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(Screen.Player.route) {
            PlayerScreen(
                onBackClick = navController::popBackStack,
                onQueueClick = { /* TODO: Handle queue click */ }
            )
        }
    }
}