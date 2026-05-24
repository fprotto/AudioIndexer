package com.unitn.audioindexer.ui.screens.albums

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.AlbumUiState
import com.unitn.audioindexer.data.sampleAlbumsState
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
    state: AlbumUiState = sampleAlbumsState()
) {
    val album = state.albums.find {
        it.id == id
    } ?: return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        album.songs.forEach { song ->
            SongCard(song)
        }
    }
}
