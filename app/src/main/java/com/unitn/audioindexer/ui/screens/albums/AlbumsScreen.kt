package com.unitn.audioindexer.ui.screens.albums

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import coil.compose.AsyncImage
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
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.ui.screens.MainScreen
import com.unitn.audioindexer.ui.screens.MiniPlayer
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.toImageVector
import com.unitn.audioindexer.ui.songs.SongCard
import com.unitn.audioindexer.ui.viewmodels.PlaylistsViewModel
import com.unitn.audioindexer.ui.components.AddToPlaylistDialog
import com.unitn.audioindexer.ui.components.CreatePlaylistDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.ui.viewmodels.AlbumsViewModel
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory

@Composable
fun AlbumsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as AudioIndexerApplication
    val repository = app.repository
    val viewModel: AlbumsViewModel = viewModel(factory = MusicViewModelFactory(repository, app.musicController))

    var searchQuery by remember { mutableStateOf("") }
    val allAlbums by viewModel.albums.collectAsState()

    val playlistViewModel: PlaylistsViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController))
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
        state = "Albums"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AlbumsControlBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )

            AlbumsSection(
                filteredAlbums,
                navController,
                onAddToQueue = { viewModel.addAlbumToQueue(it) },
                onAddToPlaylist = { showAddToPlaylistDialogForAlbum = it },
                modifier = modifier
            )
        }
    }
}

@Composable
fun AlbumsControlBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
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
                    stringResource(R.string.search_albums),
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
    }
}

@Composable
fun AlbumsSection(
    albums: List<Album>,
    navController: NavController,
    onAddToQueue: (Album) -> Unit,
    onAddToPlaylist: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        albums.sortedBy { album -> album.name }.forEach { album ->
            AlbumItem(
                album,
                navController,
                modifier = modifier,
                onAddToQueue = { onAddToQueue(album) },
                onAddToPlaylist = { onAddToPlaylist(album) }
            )
        }
    }
}

@Composable
fun AlbumItem(
    album: Album,
    navController: NavController,
    modifier: Modifier = Modifier,
    showAsCard: Boolean = false,
    onAddToQueue: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    if (showAsCard) {
        Card(
            onClick = { navController.navigate(Screen.Album.createRoute(album.id)) },
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    when (val cover = album.cover) {
                        is IconSource.VectorIcon -> Icon(
                            imageVector = cover.toImageVector(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(56.dp)
                        )
                        is IconSource.UriIcon -> {
                            AsyncImage(
                                model = cover.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = album.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = album.artist.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    AlbumMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        onMoreOptionsClick = { showMenu = true },
                        onAddToQueue = onAddToQueue,
                        onAddToPlaylist = onAddToPlaylist
                    )
                }
            }
        }
    } else {
        ListItem(
            headlineContent = {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = album.artist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    when (val cover = album.cover) {
                        is IconSource.VectorIcon -> {
                            Icon(
                                imageVector = cover.toImageVector(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        is IconSource.UriIcon -> {
                            AsyncImage(
                                model = cover.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            },
            trailingContent = {
                AlbumMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    onMoreOptionsClick = { showMenu = true },
                    onAddToQueue = onAddToQueue,
                    onAddToPlaylist = onAddToPlaylist
                )
            },
            modifier = Modifier.clickable(onClick = { navController.navigate(Screen.Album.createRoute(album.id)) })
        )
    }
}

@Composable
private fun AlbumMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    onAddToQueue: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {}
) {
    Box {
        IconButton(onClick = onMoreOptionsClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest
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
                    onDismissRequest()
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
                    onDismissRequest()
                    onAddToPlaylist()
                }
            )
        }
    }
}

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
    val viewModel: AlbumsViewModel = viewModel(factory = MusicViewModelFactory(repository, app.musicController))

    val playlistViewModel: PlaylistsViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController))
    val allPlaylists by playlistViewModel.playlists.collectAsState()

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
                    onAddToQueue = { viewModel.addAlbumToQueue(currentAlbum) },
                    onAddToPlaylistClick = { showAddToPlaylistDialogForAlbum = currentAlbum },
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
                            onAddToPlaylist = { showAddToPlaylistDialogForSong = song }
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
                        onAddToQueue = { viewModel.addAlbumToQueue(currentAlbum) },
                        onAddToPlaylistClick = { showAddToPlaylistDialogForAlbum = currentAlbum }
                    )
                }
                itemsIndexed(currentAlbum.songs) { index, song ->
                    SongCard(
                        song, 
                        onClick = { viewModel.playSong(currentAlbum.songs, index) },
                        onAddToQueue = { viewModel.addToQueue(song) },
                        onAddToPlaylist = { showAddToPlaylistDialogForSong = song }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumHeader(
    album: Album,
    songCount: Int,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onPlayClick: () -> Unit,
    onAddToQueue: () -> Unit = {},
    onAddToPlaylistClick: () -> Unit = {},
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

            // Album cover
            Box(
                modifier = Modifier
                    .size(if (isLandscape) 120.dp else 180.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                when (val cover = album.cover) {
                    is IconSource.VectorIcon -> Icon(
                        imageVector = cover.toImageVector(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(if (isLandscape) 56.dp else 80.dp)
                    )
                    is IconSource.UriIcon -> {
                        AsyncImage(
                            model = cover.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 2.dp else 8.dp)
            ) {
                // Album name
                Text(
                    text = album.name,
                    style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = if (isLandscape) 2 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )

                // Artist row
                Text(
                    text = album.artist.name,
                    style = if (isLandscape) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                // Metadata row: song count · release year
                Text(
                    text = buildString {
                        append(LocalResources.current.getQuantityString(
                            R.plurals.songs_count,
                            songCount,
                            songCount
                        ))
                        append("  ·  ")
                        append("${album.releaseYear}")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Play button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
            }
        }
    }
}
