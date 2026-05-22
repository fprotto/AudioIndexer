package com.unitn.audioindexer.ui.screens.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.data.components.PlaylistUiState
import com.unitn.audioindexer.data.samplePlaylistsState
import com.unitn.audioindexer.ui.screens.MainScreen
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
            navController = navController
        )
    }
}

@Composable
fun PlaylistSection(
    playlists: List<Playlist>,
    navController: NavController
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        playlists.forEach { playlist ->
            PlaylistItem(
                playlist,
                navController = navController
            )
        }
    }
}

@Composable
fun PlaylistItem(
    playlist: Playlist,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("playlist/${playlist.id}")
            }
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(playlist.name)
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    id: Int?,
    state: PlaylistUiState = samplePlaylistsState()
) {
    val playlist = state.playlists.filter {
        it.id == id
    }[0]

    LazyColumn {
        playlist.songs.forEach { song ->
            item {
                SongCard(song)
            }
        }
    }
}
