package com.unitn.audioindexer.ui.screens.albums

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.AlbumUiState
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.PlaylistUiState
import com.unitn.audioindexer.data.sampleAlbumsState
import com.unitn.audioindexer.data.samplePlaylistsState
import com.unitn.audioindexer.ui.screens.MainScreen
import com.unitn.audioindexer.ui.songs.SongCard

@Composable
fun AlbumsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    MainScreen(
        navController = navController,
        sampleState = "Albums"
    ) {
        AlbumsSection(
            sampleAlbumsState().albums,
            navController,
            modifier = modifier
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("album/${album.id}")
            }
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(album.name)
        }
    }
}

@Composable
fun AlbumDetailScreen(
    id: Int?,
    modifier: Modifier = Modifier,
    state: AlbumUiState = sampleAlbumsState(),
    onNavigateBack: () -> Unit = {}
) {
    val album = state.albums.find { it.id == id } ?: return

    val songCount = album.songs.size

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
                            append("$songCount ${if (songCount == 1) "song" else "songs"}")
                            append("  ·  ")
                            append("${album.releaseYear}")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Play button
                    Button(
                        onClick = { /* TODO: play album */ },
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
        items(album.songs) { song ->
            SongCard(song)
        }
    }
}
