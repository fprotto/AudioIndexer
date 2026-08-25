package com.unitn.audioindexer.ui.screens.albums

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.ui.components.dialogs.AddToPlaylistDialog
import com.unitn.audioindexer.ui.components.dialogs.CreatePlaylistDialog
import com.unitn.audioindexer.ui.screens.mainscreen.MainScreen
import com.unitn.audioindexer.ui.viewmodels.AlbumsViewModel
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.PlaylistsViewModel

@Composable
fun AlbumsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as AudioIndexerApplication
    val repository = app.repository
    val viewModel: AlbumsViewModel = viewModel(factory = MusicViewModelFactory(repository, app.musicController, app.settingsRepository))

    var searchQuery by remember { mutableStateOf("") }
    val allAlbums by viewModel.albums.collectAsState()

    val playlistViewModel: PlaylistsViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController, app.settingsRepository))
    val allPlaylists by playlistViewModel.playlists.collectAsState()

    var showAddToPlaylistDialogForSong by remember { mutableStateOf<com.unitn.audioindexer.data.components.Song?>(null) }
    var showAddToPlaylistDialogForAlbum by remember { mutableStateOf<Album?>(null) }
    var showCreateDialogForAdd by remember { mutableStateOf(false) }

    val filteredAlbums = remember(searchQuery, allAlbums) {
        allAlbums.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.artistName.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showAddToPlaylistDialogForSong != null) {
        AddToPlaylistDialog(
            playlists = allPlaylists,
            onDismissRequest = { showAddToPlaylistDialogForSong = null },
            onPlaylistSelected = { playlist ->
                viewModel.addSongToPlaylist(playlist.id, showAddToPlaylistDialogForSong!!.id)
                showAddToPlaylistDialogForSong = null
            },
            onCreateNewPlaylist = {
                showCreateDialogForAdd = true
            }
        )
    }

    if (showAddToPlaylistDialogForAlbum != null) {
        AddToPlaylistDialog(
            playlists = allPlaylists,
            onDismissRequest = { showAddToPlaylistDialogForAlbum = null },
            onPlaylistSelected = { playlist ->
                viewModel.addSongsToPlaylist(playlist.id, showAddToPlaylistDialogForAlbum!!.songs)
                showAddToPlaylistDialogForAlbum = null
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
        state = "Albums",
        modifier = modifier
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AlbumsControlBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(
            items = filteredAlbums.sortedBy { it.name.lowercase() },
            key = { album -> album.id }
        ) { album ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                AlbumItem(
                    album,
                    navController,
                    modifier = modifier,
                    onAddToQueue = { viewModel.addAlbumToQueue(album) },
                    onAddToPlaylist = { showAddToPlaylistDialogForAlbum = album }
                )
            }
        }
    }
}
