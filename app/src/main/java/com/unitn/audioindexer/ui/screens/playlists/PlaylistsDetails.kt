package com.unitn.audioindexer.ui.screens.playlists

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.ui.components.dialogs.AddToPlaylistDialog
import com.unitn.audioindexer.ui.components.dialogs.CreatePlaylistDialog
import com.unitn.audioindexer.ui.components.dialogs.DeletePlaylistConfirmationDialog
import com.unitn.audioindexer.ui.components.dialogs.EditPlaylistDialog
import com.unitn.audioindexer.ui.components.dialogs.SongSelectionDialog
import com.unitn.audioindexer.ui.screens.mainscreen.MiniPlayer
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.components.songs.SongCard
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.PlaylistsViewModel

@Composable
fun PlaylistDetailScreen(
    id: Int?,
    navController: NavController,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as AudioIndexerApplication
    val repository = app.repository
    val viewModel: PlaylistsViewModel = viewModel(factory = MusicViewModelFactory(repository, app.musicController, app.settingsRepository))
    val currentSong by viewModel.currentSong.collectAsState()

    var playlist by remember { mutableStateOf<Playlist?>(null) }
    val allPlaylists by viewModel.playlists.collectAsState()

    var showAddToPlaylistDialog by remember { mutableStateOf<com.unitn.audioindexer.data.components.Song?>(null) }
    var showCreateDialogForAdd by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showAddToPlaylistDialogForAll by remember { mutableStateOf(false) }
    var showSongSelectionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(id, allPlaylists) {
        if (id != null) {
            playlist = repository.getPlaylistById(id)
        }
    }

    val currentPlaylist = playlist ?: return

    var sortOrder by remember { mutableStateOf(PlaylistSongSortOrder.CUSTOM) }

    val sortedSongs = remember(sortOrder, currentPlaylist.songs) {
        when (sortOrder) {
            PlaylistSongSortOrder.CUSTOM -> currentPlaylist.songs // already sortedBy { it.playlistOrder } in repo
            PlaylistSongSortOrder.TITLE -> currentPlaylist.songs.sortedWith(compareBy({ it.title.lowercase() }, { it.artistName.lowercase() }))
            PlaylistSongSortOrder.ARTIST -> currentPlaylist.songs.sortedWith(compareBy( { it.artistName.lowercase() }, { it.title.lowercase() }))
            PlaylistSongSortOrder.YEAR -> currentPlaylist.songs.sortedWith(compareBy( { it.releaseYear }, { it.artistName.lowercase() }, { it.title.lowercase() }))
        }
    }

    if (showAddToPlaylistDialog != null) {
        AddToPlaylistDialog(
            playlists = allPlaylists,
            onDismissRequest = { showAddToPlaylistDialog = null },
            onPlaylistSelected = { targetPlaylist ->
                viewModel.addSongToPlaylist(targetPlaylist.id, showAddToPlaylistDialog!!.id)
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
                viewModel.createPlaylist(name)
                showCreateDialogForAdd = false
            }
        )
    }

    if (showRenameDialog) {
        EditPlaylistDialog(
            initialName = currentPlaylist.name,
            onDismissRequest = { showRenameDialog = false },
            onConfirm = { newName ->
                viewModel.renamePlaylist(currentPlaylist.id, newName)
                showRenameDialog = false
            }
        )
    }

    if (showDeleteConfirmation) {
        DeletePlaylistConfirmationDialog(
            playlistName = currentPlaylist.name,
            onDismissRequest = { showDeleteConfirmation = false },
            onConfirm = {
                viewModel.deletePlaylist(currentPlaylist.id)
                showDeleteConfirmation = false
                onNavigateBack()
            }
        )
    }

    if (showAddToPlaylistDialogForAll) {
        AddToPlaylistDialog(
            playlists = allPlaylists,
            onDismissRequest = { showAddToPlaylistDialogForAll = false },
            onPlaylistSelected = { targetPlaylist ->
                viewModel.addSongsToPlaylist(targetPlaylist.id, currentPlaylist.songs)
                showAddToPlaylistDialogForAll = false
            },
            onCreateNewPlaylist = {
                showCreateDialogForAdd = true
            }
        )
    }

    if (showSongSelectionDialog) {
        val librarySongs by viewModel.allSongs.collectAsState()
        SongSelectionDialog(
            allSongs = librarySongs,
            onDismissRequest = { showSongSelectionDialog = false },
            onConfirm = { selectedSongs ->
                viewModel.addSongsToPlaylist(currentPlaylist.id, selectedSongs)
                showSongSelectionDialog = false
            }
        )
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.updatePlaylistCover(currentPlaylist.id, uri)
        }
    }

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
                PlaylistHeader(
                    playlist = currentPlaylist,
                    songCount = currentPlaylist.songs.size,
                    onNavigateBack = onNavigateBack,
                    isLandscape = true,
                    onPlayClick = { viewModel.playPlaylist(currentPlaylist) },
                    onShuffleClick = { viewModel.playPlaylist(currentPlaylist, shuffle = true) },
                    onUpdateCoverClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onRemoveCoverClick = { viewModel.removePlaylistCover(currentPlaylist.id) },
                    onAddSongsClick = { showSongSelectionDialog = true },
                    onAddToQueue = { viewModel.addPlaylistToQueue(currentPlaylist) },
                    onRenameClick = { showRenameDialog = true },
                    onDeleteClick = { showDeleteConfirmation = true },
                    sortOrder = sortOrder,
                    onSortOrderChange = { sortOrder = it },
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                ) {
                    itemsIndexed(sortedSongs) { index, song ->
                        SongCard(
                            song,
                            onClick = { viewModel.playSong(sortedSongs, index) },
                            onAddToQueue = { viewModel.addToQueue(song) },
                            onAddToPlaylist = { showAddToPlaylistDialog = song },
                            onPropertiesClick = { navController.navigate(Screen.SongProperties.createRoute(song.id)) },
                            onRemoveFromPlaylist = { viewModel.removeSongFromPlaylist(currentPlaylist.id, song.id) },
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
                    PlaylistHeader(
                        playlist = currentPlaylist,
                        songCount = currentPlaylist.songs.size,
                        onNavigateBack = onNavigateBack,
                        onPlayClick = { viewModel.playPlaylist(currentPlaylist) },
                        onShuffleClick = { viewModel.playPlaylist(currentPlaylist, shuffle = true) },
                        onUpdateCoverClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onRemoveCoverClick = { viewModel.removePlaylistCover(currentPlaylist.id) },
                        onAddSongsClick = { showSongSelectionDialog = true },
                        onAddToQueue = { viewModel.addPlaylistToQueue(currentPlaylist) },
                        onRenameClick = { showRenameDialog = true },
                        onDeleteClick = { showDeleteConfirmation = true },
                        sortOrder = sortOrder,
                        onSortOrderChange = { sortOrder = it }
                    )
                }
                itemsIndexed(sortedSongs) { index, song ->
                    SongCard(
                        song,
                        onClick = { viewModel.playSong(sortedSongs, index) },
                        onAddToQueue = { viewModel.addToQueue(song) },
                        onAddToPlaylist = { showAddToPlaylistDialog = song },
                        onPropertiesClick = { navController.navigate(Screen.SongProperties.createRoute(song.id)) },
                        onRemoveFromPlaylist = { viewModel.removeSongFromPlaylist(currentPlaylist.id, song.id) },
                        onDelete = { viewModel.deleteSong(song) },
                        isPlaying = song.id == currentSong?.id
                    )
                }
            }
        }
    }
}
