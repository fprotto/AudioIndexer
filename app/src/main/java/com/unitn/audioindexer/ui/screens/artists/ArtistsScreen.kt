package com.unitn.audioindexer.ui.screens.artists

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PlaylistAddCircle
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.AlbumUiState
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.data.components.ArtistUiState
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.sampleAlbumsState
import com.unitn.audioindexer.data.sampleArtistsState
import com.unitn.audioindexer.ui.screens.MainScreen
import com.unitn.audioindexer.ui.screens.MiniPlayer
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.screens.albums.AlbumItem
import com.unitn.audioindexer.ui.songs.SongCard

@Composable
fun ArtistsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val allArtists = remember { sampleArtistsState().artists }

    val filteredArtists = remember(searchQuery, allArtists) {
        allArtists.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    MainScreen(
        navController = navController,
        sampleState = "Artists"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ArtistsControlBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )

            ArtistSection(
                filteredArtists,
                navController,
                modifier = modifier
            )
        }

    }
}

@Composable
fun ArtistsControlBar(
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
                    stringResource(R.string.search_artists),
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
fun ArtistSection(
    artists: List<Artist>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        artists.forEach { artist ->
            ArtistItem(
                artist,
                navController,
                modifier = modifier
            )
        }
    }
}

@Composable
fun ArtistItem(
    artist: Artist,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
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
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        trailingContent = {
            ArtistMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                onMoreOptionsClick = { showMenu = true }
            )
        },
        modifier = Modifier.clickable(onClick = { navController.navigate(Screen.Artist.createRoute(artist.id)) })
    )
}

@Composable
fun ArtistDetailScreen(
    id: Int?,
    navController: NavController,
    modifier: Modifier = Modifier,
    state: ArtistUiState = sampleArtistsState(),
    albumsState: AlbumUiState = sampleAlbumsState(),
    onNavigateBack: () -> Unit = {}
) {
    val artist = state.artists.find {
        it.id == id
    } ?: return

    val artistAlbums = albumsState.albums.filter {
        it.artist.id == artist.id
    }

    val topSongs = artistAlbums.flatMap { it.songs }
        .sortedByDescending { it.playCount }
        .take(10)

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

                    ArtistHeader(
                        artist = artist,
                        modifier = Modifier
                            .padding(top = 56.dp, bottom = 24.dp)
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            if (topSongs.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.favorite_songs),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 24.dp, bottom = 8.dp)
                    )
                }

                items(topSongs) { song ->
                    SongCard(song = song)
                }
            }

            if (artistAlbums.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.albums_header),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 24.dp, bottom = 16.dp)
                    )
                }

                val chunkedAlbums = artistAlbums.chunked(2)
                items(chunkedAlbums) { rowAlbums ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowAlbums.forEach { album ->
                            Box(modifier = Modifier.weight(1f)) {
                                AlbumItem(
                                    album = album,
                                    navController = navController,
                                    showAsCard = true
                                )
                            }
                        }
                        if (rowAlbums.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistHeader(
    artist: Artist,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            when (val propic = artist.propic) {
                is IconSource.VectorIcon -> Icon(
                    imageVector = propic.imageVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(60.dp)
                )
                is IconSource.BitmapIcon -> Image(
                    bitmap = propic.bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Text(
            text = artist.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ArtistMenu(
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
