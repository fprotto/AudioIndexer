package com.unitn.audioindexer.ui.screens.albums

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.data.sampleAlbumsState
import com.unitn.audioindexer.ui.MainScreen

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
            navController
        )
    }
}

@Composable
fun AlbumsSection(
    albums: List<Album>,
    navController: NavController
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        albums.forEach { album ->
            AlbumItem(
                album,
                navController
            )
        }
    }
}

@Composable
fun AlbumItem(
    album: Album,
    navController: NavController
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
fun AlbumDetailScreen(id: String?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Album ID: $id")
    }
}
