package com.unitn.audioindexer.ui.screens.tracks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.unitn.audioindexer.R
import androidx.navigation.NavController
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.ui.screens.MainScreen
import com.unitn.audioindexer.ui.songs.SongCard
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.TracksViewModel
import com.unitn.audioindexer.ui.viewmodels.PlaylistsViewModel
import com.unitn.audioindexer.ui.components.AddToPlaylistDialog
import com.unitn.audioindexer.ui.components.CreatePlaylistDialog

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
    val viewModel: TracksViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController))
    
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SongSortOrder.TITLE) }

    val allSongs by viewModel.songs.collectAsState()
    
    val playlistViewModel: PlaylistsViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController))
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
            SongSortOrder.TITLE -> filteredSongs.sortedBy { it.title }
            SongSortOrder.ARTIST -> filteredSongs.sortedBy { it.artistName }
            SongSortOrder.YEAR -> filteredSongs.sortedByDescending { it.releaseYear }
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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            
            TracksSection(
                sortedSongs,
                onSongClick = { index -> viewModel.playSong(sortedSongs, index) },
                onAddToQueue = { song -> viewModel.addToQueue(song) },
                onAddToPlaylist = { song -> showAddToPlaylistDialog = song }
            )
        }
    }
}

@Composable
fun TracksControlBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onShuffleClick: () -> Unit,
    sortOrder: SongSortOrder,
    onSortOrderChange: (SongSortOrder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            placeholder = {
                Text(
                    stringResource(R.string.search_songs),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        )

        IconButton(onClick = onShuffleClick, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.Default.Shuffle,
                contentDescription = stringResource(R.string.shuffle),
                modifier = Modifier.size(20.dp)
            )
        }

        Box {
            IconButton(onClick = { expanded = true }, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = stringResource(R.string.sort),
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.title)) },
                    onClick = { onSortOrderChange(SongSortOrder.TITLE); expanded = false },
                    trailingIcon = {
                        if (sortOrder == SongSortOrder.TITLE) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.artist)) },
                    onClick = { onSortOrderChange(SongSortOrder.ARTIST); expanded = false },
                    trailingIcon = {
                        if (sortOrder == SongSortOrder.ARTIST) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.release_year)) },
                    onClick = { onSortOrderChange(SongSortOrder.YEAR); expanded = false },
                    trailingIcon = {
                        if (sortOrder == SongSortOrder.YEAR) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TracksSection(
    songs: List<Song>,
    onSongClick: (Int) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        songs.forEachIndexed { index, song ->
            SongCard(
                song, 
                onClick = { onSongClick(index) },
                onAddToQueue = { onAddToQueue(song) },
                onAddToPlaylist = { onAddToPlaylist(song) }
            )
        }
    }
}
