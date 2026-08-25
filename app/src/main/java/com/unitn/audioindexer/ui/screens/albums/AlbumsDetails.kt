package com.unitn.audioindexer.ui.screens.albums

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.ui.components.dialogs.AddToPlaylistDialog
import com.unitn.audioindexer.ui.components.dialogs.CreatePlaylistDialog
import com.unitn.audioindexer.ui.screens.mainscreen.MiniPlayer
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.components.songs.SongCard
import com.unitn.audioindexer.ui.viewmodels.AlbumsViewModel
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.PlaylistsViewModel

@Composable
fun AlbumDetailScreen(
    id: Int?,
    navController: NavController,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as AudioIndexerApplication
    val repository = app.repository
    val viewModel: AlbumsViewModel = viewModel(factory = MusicViewModelFactory(repository, app.musicController, app.settingsRepository))

    val playlistViewModel: PlaylistsViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController, app.settingsRepository))
    val allPlaylists by playlistViewModel.playlists.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    var showAddToPlaylistDialogForSong by remember { mutableStateOf<com.unitn.audioindexer.data.components.Song?>(null) }
    var showAddToPlaylistDialogForAlbum by remember { mutableStateOf<Album?>(null) }
    var showCreateDialogForAdd by remember { mutableStateOf(false) }

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

    var album by remember { mutableStateOf<Album?>(null) }

    LaunchedEffect(id) {
        if (id != null) {
            album = repository.getAlbumById(id)
        }
    }

    val currentAlbum = album ?: return

    val songCount = currentAlbum.songs.size
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        bottomBar = {
            MiniPlayer(
                onClick = { navController.navigate(Screen.Player.route) }
            )
        }
    ) { padding ->
        if (isLandscape) {
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                AlbumHeader(
                    album = currentAlbum,
                    songCount = songCount,
                    onNavigateBack = onNavigateBack,
                    isLandscape = true,
                    onPlayClick = { viewModel.playAlbum(currentAlbum) },
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                ) {
                    itemsIndexed(currentAlbum.songs) { index, song ->
                        SongCard(
                            song,
                            onClick = { viewModel.playSong(currentAlbum.songs, index) },
                            onAddToQueue = { viewModel.addToQueue(song) },
                            onAddToPlaylist = { showAddToPlaylistDialogForSong = song },
                            onPropertiesClick = { navController.navigate(Screen.SongProperties.createRoute(song.id)) },
                            onDelete = { viewModel.deleteSong(song) },
                            isPlaying = song.id == currentSong?.id
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                item {
                    AlbumHeader(
                        album = currentAlbum,
                        songCount = songCount,
                        onNavigateBack = onNavigateBack,
                        onPlayClick = { viewModel.playAlbum(currentAlbum) },
                    )
                }
                itemsIndexed(currentAlbum.songs) { index, song ->
                    SongCard(
                        song,
                        onClick = { viewModel.playSong(currentAlbum.songs, index) },
                        onAddToQueue = { viewModel.addToQueue(song) },
                        onAddToPlaylist = { showAddToPlaylistDialogForSong = song },
                        onPropertiesClick = { navController.navigate(Screen.SongProperties.createRoute(song.id)) },
                        onDelete = { viewModel.deleteSong(song) },
                        isPlaying = song.id == currentSong?.id
                    )
                }
            }
        }
    }
}
