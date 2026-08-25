package com.unitn.audioindexer.ui.screens.tracks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.ui.components.dialogs.AddToPlaylistDialog
import com.unitn.audioindexer.ui.components.dialogs.CreatePlaylistDialog
import com.unitn.audioindexer.ui.screens.mainscreen.MainScreen
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.components.songs.SongCard
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.PlaylistsViewModel
import com.unitn.audioindexer.ui.viewmodels.TracksViewModel

enum class SongSortOrder {
    TITLE, ARTIST, YEAR
}

@Composable
fun TracksScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as AudioIndexerApplication
    val viewModel: TracksViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController, app.settingsRepository))
    
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SongSortOrder.TITLE) }

    val allSongs by viewModel.songs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    
    val playlistViewModel: PlaylistsViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController, app.settingsRepository))
    val allPlaylists by playlistViewModel.playlists.collectAsState()

    var showAddToPlaylistDialog by remember { mutableStateOf<Song?>(null) }
    var showCreateDialogForAdd by remember { mutableStateOf(false) }

    val filteredSongs = remember(searchQuery, allSongs) {
        allSongs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artistName.contains(searchQuery, ignoreCase = true)
        }
    }

    val sortedSongs = remember(sortOrder, filteredSongs) {
        when (sortOrder) {
            SongSortOrder.TITLE -> filteredSongs.sortedBy { it.title.lowercase() }
            SongSortOrder.ARTIST -> filteredSongs.sortedWith(compareBy({ it.artistName.lowercase() }, { it.title.lowercase() }))
            SongSortOrder.YEAR -> filteredSongs.sortedWith(compareBy({ it.releaseYear }, { it.artistName.lowercase() }, { it.title.lowercase() }))
        }
    }

    if (showAddToPlaylistDialog != null) {
        AddToPlaylistDialog(
            playlists = allPlaylists,
            onDismissRequest = { showAddToPlaylistDialog = null },
            onPlaylistSelected = { playlist ->
                viewModel.addSongToPlaylist(playlist.id, showAddToPlaylistDialog!!.id)
                showAddToPlaylistDialog = null
            },
            onCreateNewPlaylist = {
                showCreateDialogForAdd = true
            }
        )
    }

    if (showCreateDialogForAdd) {
        CreatePlaylistDialog(
            onDismissRequest = { showCreateDialogForAdd = false },
            onConfirm = { name ->
                playlistViewModel.createPlaylist(name)
                showCreateDialogForAdd = false
            }
        )
    }

    MainScreen(
        navController = navController,
        state = "Tracks",
        modifier = modifier
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                TracksControlBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onShuffleClick = { 
                        if (sortedSongs.isNotEmpty()) {
                            viewModel.playSong(sortedSongs.shuffled(), 0)
                        }
                    },
                    sortOrder = sortOrder,
                    onSortOrderChange = { sortOrder = it }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        itemsIndexed(
            items = sortedSongs,
            key = { _, song -> song.id }
        ) { index, song ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                SongCard(
                    song, 
                    onClick = { viewModel.playSong(sortedSongs, index) },
                    onAddToQueue = { viewModel.addToQueue(song) },
                    onAddToPlaylist = { showAddToPlaylistDialog = song },
                    onPropertiesClick = { navController.navigate(Screen.SongProperties.createRoute(song.id)) },
                    onDelete = { viewModel.deleteSong(song) },
                    isPlaying = song.id == currentSong?.id
                )
            }
        }
    }
}
