package com.unitn.audioindexer.ui.screens.artists

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.ui.screens.mainscreen.MiniPlayer
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.screens.albums.AlbumItem
import com.unitn.audioindexer.ui.components.songs.SongCard
import com.unitn.audioindexer.ui.viewmodels.ArtistsViewModel
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.collections.chunked

@Composable
fun ArtistDetailScreen(
    id: Int?,
    navController: NavController,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as AudioIndexerApplication
    val repository = app.repository
    val viewModel: ArtistsViewModel = viewModel(factory = MusicViewModelFactory(repository, app.musicController, app.settingsRepository))
    val currentSong by viewModel.currentSong.collectAsState()

    var artist by remember { mutableStateOf<Artist?>(null) }
    var artistAlbums by remember { mutableStateOf<List<Album>>(emptyList()) }

    val allArtistSongs by remember(id) {
        if (id != null) {
            repository.getSongsByArtist(id)
                .map { songs -> songs.sortedByDescending { it.playCount } }
        } else {
            flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    LaunchedEffect(id) {
        if (id != null) {
            artist = repository.getArtistById(id)
            artistAlbums = repository.allAlbums.first().filter { it.artist.id == id }
        }
    }

    val currentArtist = artist ?: return

    val topSongs = allArtistSongs.take(5)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2

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
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    ArtistHeader(
                        artist = currentArtist,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Back button pinned at the top
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                            tint = Color.White
                        )
                    }
                }
            }

            if (topSongs.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.most_listened_songs),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 24.dp, bottom = 8.dp)
                    )
                }

                itemsIndexed(topSongs) { index, song ->
                    SongCard(
                        song = song,
                        onClick = { viewModel.playSong(allArtistSongs, index) },
                        onAddToQueue = { viewModel.addToQueue(song) },
                        onPropertiesClick = { navController.navigate(Screen.SongProperties.createRoute(song.id)) },
                        onDelete = { viewModel.deleteSong(song) },
                        isPlaying = song.id == currentSong?.id
                    )
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

                val chunkedAlbums = artistAlbums.sortedByDescending { album -> album.releaseYear }.chunked(columns)
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
                                    showAsCard = true,
                                    onAddToQueue = { viewModel.addAlbumToQueue(album) }
                                )
                            }
                        }
                        repeat(columns - rowAlbums.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
