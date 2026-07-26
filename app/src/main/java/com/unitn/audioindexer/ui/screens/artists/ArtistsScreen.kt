package com.unitn.audioindexer.ui.screens.artists

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.ui.screens.MainScreen
import com.unitn.audioindexer.ui.screens.MiniPlayer
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.screens.albums.AlbumItem
import com.unitn.audioindexer.ui.songs.SongCard
import com.unitn.audioindexer.ui.toImageVector
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.ui.viewmodels.ArtistsViewModel
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape

@Composable
fun ArtistsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as AudioIndexerApplication
    val viewModel: ArtistsViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController))

    var searchQuery by remember { mutableStateOf("") }
    val allArtists by viewModel.artists.collectAsState()

    val filteredArtists = remember(searchQuery, allArtists) {
        allArtists.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    MainScreen(
        navController = navController,
        state = "Artists"
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
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                when (val propic = artist.propic) {
                    is IconSource.VectorIcon -> Icon(
                        imageVector = propic.toImageVector(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    is IconSource.UriIcon -> AsyncImage(
                        model = propic.uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        modifier = Modifier.clickable(onClick = { navController.navigate(Screen.Artist.createRoute(artist.id)) })
    )
}

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
    val viewModel: ArtistsViewModel = viewModel(factory = MusicViewModelFactory(repository, app.musicController))
    
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
                        text = stringResource(R.string.favorite_songs),
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
                        onDelete = { viewModel.deleteSong(song) }
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

@Composable
fun ArtistHeader(
    artist: Artist,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        // Hero Image
        when (val propic = artist.propic) {
            is IconSource.VectorIcon -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = propic.toImageVector(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(120.dp)
                )
            }
            is IconSource.UriIcon -> AsyncImage(
                model = propic.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Gradient Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 300f
                    )
                )
        )

        // Artist Name
        Text(
            text = artist.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}
