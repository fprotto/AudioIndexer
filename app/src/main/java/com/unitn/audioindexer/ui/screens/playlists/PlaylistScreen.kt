package com.unitn.audioindexer.ui.screens.playlists

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.data.components.PlaylistUiState
import com.unitn.audioindexer.data.samplePlaylistsState
import com.unitn.audioindexer.ui.screens.MainScreen
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.songs.SongCard
import kotlin.collections.forEach

@Composable
fun PlaylistsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    MainScreen(
        navController = navController,
        sampleState = "Playlists"
    ) {
        PlaylistSection(
            samplePlaylistsState().playlists,
            navController = navController,
            modifier = modifier
        )
    }
}

@Composable
fun PlaylistSection(
    playlists: List<Playlist>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // We avoid using LazyVerticalGrid here because it is nested inside another LazyColumn
    // in MainScreen, which would cause an IllegalStateException due to infinite height constraints.
    // Instead, we manually create a grid using Column and Row.
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        playlists.chunked(2).forEach { rowPlaylists ->
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
                // Add an empty spacer if the row is not full to maintain grid alignment
                if (rowPlaylists.size < 2) {
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
                    text = "$songCount ${if (songCount == 1) "song" else "songs"}",
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
    modifier: Modifier = Modifier,
    state: PlaylistUiState = samplePlaylistsState(),
    onNavigateBack: () -> Unit = {}
) {
    val playlist = state.playlists.find { it.id == id } ?: return

    val songCount = playlist.songs.size
    //val artistCount = playlist.songs.map { it.artist }.distinctBy { it.name }.size

    LazyColumn(modifier = modifier.fillMaxSize()) {

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
                        contentDescription = "Navigate back",
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
                    // Playlist cover
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        when (val cover = playlist.cover) {
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

                    // Playlist name
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    // Metadata row: song count · artist count
                    Text(
                        text = buildString {
                            append("$songCount ${if (songCount == 1) "song" else "songs"}")
                            //append("  ·  ")
                            //append("$artistCount ${if (artistCount == 1) "artist" else "artists"}")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Play button
                    Button(
                        onClick = { /* TODO: play playlist */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Play")
                    }
                }
            }
        }

        // Song list
        items(playlist.songs) { song ->
            SongCard(song)
        }
    }
}
