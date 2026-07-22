package com.unitn.audioindexer.ui.screens.playlists

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.ui.screens.MainScreen
import com.unitn.audioindexer.ui.screens.MiniPlayer
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.toImageVector
import com.unitn.audioindexer.ui.songs.SongCard
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unitn.audioindexer.AudioIndexerApplication
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.PlaylistsViewModel
import com.unitn.audioindexer.ui.components.CreatePlaylistDialog
import com.unitn.audioindexer.ui.components.AddToPlaylistDialog
import com.unitn.audioindexer.ui.components.EditPlaylistDialog
import com.unitn.audioindexer.ui.components.DeletePlaylistConfirmationDialog
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check

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
    val viewModel: PlaylistsViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController))

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

    MainScreen(
        navController = navController,
        state = "Playlists"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PlaylistsControlBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onNewPlaylistClick = { showCreateDialog = true }
            )

            PlaylistSection(
                filteredPlaylists,
                navController = navController,
                modifier = modifier
            )
        }

    }
}

@Composable
fun PlaylistsControlBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNewPlaylistClick: () -> Unit
) {
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
                    stringResource(R.string.search_playlists),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.search),
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

        IconButton(onClick = onNewPlaylistClick, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.PlaylistAdd,
                contentDescription = stringResource(R.string.new_playlist),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PlaylistSection(
    playlists: List<Playlist>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        playlists.chunked(columns).forEach { rowPlaylists ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
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

@Composable
fun PlaylistItem(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            // Cover
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                when (val cover = playlist.cover) {
                    is IconSource.VectorIcon -> Icon(
                        imageVector = cover.toImageVector(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(56.dp)
                    )
                    is IconSource.UriIcon -> {
                        // TODO: Use Coil
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            // Name and song count
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val songCount = playlist.songs.size
                Text(
                    text = LocalResources.current.getQuantityString(
                        R.plurals.songs_count,
                        songCount,
                        songCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

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
    val viewModel: PlaylistsViewModel = viewModel(factory = MusicViewModelFactory(repository, app.musicController))
    
    var playlist by remember { mutableStateOf<Playlist?>(null) }
    val allPlaylists by viewModel.playlists.collectAsState()

    var showAddToPlaylistDialog by remember { mutableStateOf<com.unitn.audioindexer.data.components.Song?>(null) }
    var showCreateDialogForAdd by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showAddToPlaylistDialogForAll by remember { mutableStateOf(false) }
    
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
            PlaylistSongSortOrder.TITLE -> currentPlaylist.songs.sortedBy { it.title }
            PlaylistSongSortOrder.ARTIST -> currentPlaylist.songs.sortedBy { it.artistName }
            PlaylistSongSortOrder.YEAR -> currentPlaylist.songs.sortedByDescending { it.releaseYear }
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
                    onAddToQueue = { viewModel.addPlaylistToQueue(currentPlaylist) },
                    onRenameClick = { showRenameDialog = true },
                    onDeleteClick = { showDeleteConfirmation = true },
                    onAddToPlaylistClick = { showAddToPlaylistDialogForAll = true },
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
                            onRemoveFromPlaylist = { viewModel.removeSongFromPlaylist(currentPlaylist.id, song.id) }
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
                        onAddToQueue = { viewModel.addPlaylistToQueue(currentPlaylist) },
                        onRenameClick = { showRenameDialog = true },
                        onDeleteClick = { showDeleteConfirmation = true },
                        onAddToPlaylistClick = { showAddToPlaylistDialogForAll = true },
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
                        onRemoveFromPlaylist = { viewModel.removeSongFromPlaylist(currentPlaylist.id, song.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistHeader(
    playlist: Playlist,
    songCount: Int,
    onNavigateBack: () -> Unit,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddToQueue: () -> Unit = {},
    onRenameClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onAddToPlaylistClick: () -> Unit = {},
    sortOrder: PlaylistSongSortOrder = PlaylistSongSortOrder.CUSTOM,
    onSortOrderChange: (PlaylistSongSortOrder) -> Unit = {},
    isLandscape: Boolean = false
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .statusBarsPadding()
    ) {
        // Back button pinned at the top
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = if (isLandscape) 40.dp else 56.dp,
                    bottom = if (isLandscape) 16.dp else 24.dp
                )
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isLandscape) Arrangement.SpaceEvenly else Arrangement.spacedBy(16.dp)
        ) {
            if (isLandscape) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Playlist cover
            Box(
                modifier = Modifier
                    .size(if (isLandscape) 120.dp else 180.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                when (val cover = playlist.cover) {
                    is IconSource.VectorIcon -> Icon(
                        imageVector = cover.toImageVector(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(if (isLandscape) 56.dp else 80.dp)
                    )
                    is IconSource.UriIcon -> {
                        // TODO: Use Coil
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(if (isLandscape) 56.dp else 80.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 4.dp else 8.dp)
            ) {
                // Playlist name
                Text(
                    text = playlist.name,
                    style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = if (isLandscape) 2 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )

                // Metadata row: song count
                Text(
                    text = buildString {
                        append(LocalResources.current.getQuantityString(
                            R.plurals.songs_count,
                            songCount,
                            songCount
                        ))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action buttons row
            var showMenu by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play button
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = if (isLandscape) ButtonDefaults.TextButtonContentPadding else ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.play),
                        modifier = Modifier.size(if (isLandscape) 18.dp else ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(if (isLandscape) 4.dp else ButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(R.string.play),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = if (isLandscape) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge
                    )
                }

                // Shuffle button
                FilledTonalButton(
                    onClick = onShuffleClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = if (isLandscape) ButtonDefaults.TextButtonContentPadding else ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = stringResource(R.string.shuffle_play),
                        modifier = Modifier.size(if (isLandscape) 18.dp else ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(if (isLandscape) 4.dp else ButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(R.string.shuffle_play),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = if (isLandscape) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge
                    )
                }

                // More options
                var sortExpanded by remember { mutableStateOf(false) }

                Box {
                    IconButton(onClick = { sortExpanded = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.sort)
                        )
                    }
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.custom_order)) },
                            onClick = { onSortOrderChange(PlaylistSongSortOrder.CUSTOM); sortExpanded = false },
                            trailingIcon = {
                                if (sortOrder == PlaylistSongSortOrder.CUSTOM) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.title)) },
                            onClick = { onSortOrderChange(PlaylistSongSortOrder.TITLE); sortExpanded = false },
                            trailingIcon = {
                                if (sortOrder == PlaylistSongSortOrder.TITLE) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.artist)) },
                            onClick = { onSortOrderChange(PlaylistSongSortOrder.ARTIST); sortExpanded = false },
                            trailingIcon = {
                                if (sortOrder == PlaylistSongSortOrder.ARTIST) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.release_year)) },
                            onClick = { onSortOrderChange(PlaylistSongSortOrder.YEAR); sortExpanded = false },
                            trailingIcon = {
                                if (sortOrder == PlaylistSongSortOrder.YEAR) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AddToQueue,
                                    contentDescription = stringResource(R.string.menu_add_to_queue),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(stringResource(R.string.menu_add_to_queue)) },
                            onClick = {
                                showMenu = false
                                onAddToQueue()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PlaylistAddCircle,
                                    contentDescription = stringResource(R.string.menu_add_to_playlist),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(stringResource(R.string.menu_add_to_playlist)) },
                            onClick = { 
                                showMenu = false
                                onAddToPlaylistClick()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DriveFileRenameOutline,
                                    contentDescription = stringResource(R.string.menu_rename),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(stringResource(R.string.menu_rename)) },
                            onClick = {
                                showMenu = false
                                onRenameClick()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(R.string.menu_properties),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(stringResource(R.string.menu_properties)) },
                            onClick = { showMenu = false /* TODO: implement */ }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.menu_delete_playlist),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            text = { 
                                Text(
                                    stringResource(R.string.menu_delete_playlist),
                                    color = MaterialTheme.colorScheme.error
                                ) 
                            },
                            onClick = { 
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}
