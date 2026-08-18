package com.unitn.audioindexer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.screens.albums.AlbumDetailScreen
import com.unitn.audioindexer.ui.screens.albums.AlbumsScreen
import com.unitn.audioindexer.ui.screens.artists.ArtistDetailScreen
import com.unitn.audioindexer.ui.screens.artists.ArtistsScreen
import com.unitn.audioindexer.ui.screens.player.LyricsScreen
import com.unitn.audioindexer.ui.screens.player.PlayerScreen
import com.unitn.audioindexer.ui.screens.player.PlayerSettingsScreen
import com.unitn.audioindexer.ui.screens.player.QueueScreen
import com.unitn.audioindexer.ui.screens.playlists.PlaylistDetailScreen
import com.unitn.audioindexer.ui.screens.playlists.PlaylistsScreen
import com.unitn.audioindexer.ui.screens.setup.SetupScreen
import com.unitn.audioindexer.ui.screens.songs.SongPropertiesScreen
import com.unitn.audioindexer.ui.screens.tracks.TracksScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Setup.route) {
            SetupScreen(onSetupComplete = {
                navController.navigate(Screen.Tracks.route) {
                    popUpTo(Screen.Setup.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Tracks.route) {
            TracksScreen(navController)
        }

        composable(Screen.SongProperties.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("songId")?.toInt()
            SongPropertiesScreen(
                id = id,
                onNavigateBack = { navController.popBackStack() }
            )
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
                navController = navController,
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
                navController = navController,
                onNavigateBack = navController::popBackStack
            )
        }

        composable(Screen.Player.route) {
            PlayerScreen(
                onBackClick = navController::popBackStack,
                onQueueClick = { navController.navigate(Screen.Queue.route) },
                onLyricsClick = { navController.navigate(Screen.Lyrics.route) },
                onSettingsClick = { navController.navigate(Screen.PlayerSettings.route) }
            )
        }

        composable(Screen.PlayerSettings.route) {
            PlayerSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Queue.route) {
            QueueScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Lyrics.route) {
            LyricsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}