package com.unitn.audioindexer.ui.screens.artists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.data.components.AlbumUiState
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.data.components.ArtistUiState
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.data.sampleAlbumsState
import com.unitn.audioindexer.data.sampleArtistsState
import com.unitn.audioindexer.ui.screens.MainScreen

@Composable
fun ArtistsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    MainScreen(
        navController = navController,
        sampleState = "Artists"
    ) {
        ArtistSection(
            sampleArtistsState().artists,
            navController,
            modifier = modifier
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("artist/${artist.id}")
            }
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(artist.name)
        }
    }
}

@Composable
fun ArtistDetailScreen(
    id: Int?,
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

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        artistAlbums.forEach { album ->
            val playlist = Playlist(
                id = album.id,
                name = album.name,
                cover = album.cover,
                songs = album.songs
            )

            Column {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                //PlaylistDetailContent(playlist)
            }
        }
    }
}
