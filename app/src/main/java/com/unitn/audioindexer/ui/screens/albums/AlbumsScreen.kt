package com.unitn.audioindexer.ui.screens.albums

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material.icons.filled.Album
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.AlbumUiState
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.sampleAlbumsState
import com.unitn.audioindexer.ui.screens.MainScreen
import com.unitn.audioindexer.ui.screens.MiniPlayer
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.songs.SongCard

@Composable
fun AlbumsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val allAlbums = remember { sampleAlbumsState().albums }

    val filteredAlbums = remember(searchQuery, allAlbums) {
        allAlbums.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.artist.name.contains(searchQuery, ignoreCase = true)
        }
    }

    MainScreen(
        navController = navController,
        sampleState = "Albums"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AlbumsControlBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )

            AlbumsSection(
                filteredAlbums,
                navController,
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
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        albums.forEach { album ->
            AlbumItem(
                album,
                navController,
                modifier = modifier
            )
        }
    }
}

@Composable
fun AlbumItem(
    album: Album,
    navController: NavController,
    modifier: Modifier = Modifier,
    showAsCard: Boolean = false
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
                            imageVector = cover.imageVector,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(56.dp)
                        )
                        is IconSource.BitmapIcon -> Image(
                            bitmap = cover.bitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
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
                        onMoreOptionsClick = { showMenu = true }
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
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            trailingContent = {
                AlbumMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    onMoreOptionsClick = { showMenu = true }
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
    onMoreOptionsClick: () -> Unit
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
                    // TODO: implement add to queue
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
                    // TODO: implement add to playlist
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
    state: AlbumUiState = sampleAlbumsState(),
    onNavigateBack: () -> Unit = {}
) {
    val album = state.albums.find { it.id == id } ?: return

    val songCount = album.songs.size

    Scaffold(
        bottomBar = {
            MiniPlayer(
                onClick = { navController.navigate(Screen.Player.route) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Cover + title + metadata block
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Back button pinned at the top
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(8.dp)
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
                            .padding(top = 56.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Album cover
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            when (val cover = album.cover) {
                                is IconSource.VectorIcon -> Icon(
                                    imageVector = cover.imageVector,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(80.dp)
                                )
                                is IconSource.BitmapIcon -> Image(
                                    bitmap = cover.bitmap,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Album name
                        Text(
                            text = album.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Artist row
                        Text(
                            text = album.artist.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
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

                        // Play button + Menu
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { /* TODO: play album */ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = stringResource(R.string.play),
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                                Text(stringResource(R.string.play))
                            }

                            var showMenu by remember { mutableStateOf(false) }
                            AlbumMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                onMoreOptionsClick = { showMenu = true }
                            )
                        }
                    }
                }
            }

            // Song list
            items(album.songs) { song ->
                SongCard(song)
            }
        }
    }
}
