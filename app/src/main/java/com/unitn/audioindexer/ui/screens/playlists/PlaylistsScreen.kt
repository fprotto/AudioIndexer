package com.unitn.audioindexer.ui.screens.playlists

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.ui.components.dialogs.CreatePlaylistDialog
import com.unitn.audioindexer.ui.screens.mainscreen.MainScreen
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.PlaylistsViewModel

enum class PlaylistSongSortOrder {
    CUSTOM, TITLE, ARTIST, YEAR
}

@Composable
fun PlaylistsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as AudioIndexerApplication
    val viewModel: PlaylistsViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController, app.settingsRepository))

    var searchQuery by remember { mutableStateOf("") }
    val allPlaylists by viewModel.playlists.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredPlaylists = remember(searchQuery, allPlaylists) {
        allPlaylists.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismissRequest = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2

    MainScreen(
        navController = navController,
        state = "Playlists",
        modifier = modifier
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                PlaylistsControlBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onNewPlaylistClick = { showCreateDialog = true }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        val chunkedPlaylists = filteredPlaylists.chunked(columns)

        items(chunkedPlaylists) { rowPlaylists ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                rowPlaylists.forEach { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onClick = { navController.navigate(Screen.Playlist.createRoute(playlist.id)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Add empty spacers if the row is not full to maintain grid alignment
                repeat(columns - rowPlaylists.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
