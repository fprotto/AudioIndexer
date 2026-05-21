package com.unitn.audioindexer.ui.screen.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.sampleHomeState
import com.unitn.audioindexer.ui.components.Playlist
import com.unitn.audioindexer.ui.components.Song
import com.unitn.audioindexer.ui.components.Source

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Songs : Screen("songs")
    object Folders : Screen("folders")
    object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        fun createRoute(id: Int) = "playlist_detail/$id"
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val state = remember { sampleHomeState() }

    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = { MiniPlayer() }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item { QuickAccessSection(navController) }

            item { SectionTitle("Playlists") }
            item { PlaylistSection(state.playlists, navController) }

            item { SectionTitle("Sources") }
            item { SourcesSection(state.sources) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}

@Composable
fun QuickAccessSection(navController: NavController) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickChip("Songs") {
            navController.navigate("songs")
        }
        QuickChip("Folders") {
            navController.navigate("folders")
        }
    }
}

@Composable
fun QuickChip(label: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
fun RecentSongsSection(songs: List<Song>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(songs) { song ->
            SongCard(song)
        }
    }
}

@Composable
fun SongCard(song: Song) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            Text(song.title, maxLines = 1)
            Text(
                song.artist,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PlaylistSection(playlists: List<Playlist>, navController: NavController) {
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
fun PlaylistItem(playlist: Playlist, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("playlist_detail/${playlist.id}")
            }
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(playlist.name)
        }
    }
}

@Composable
fun SourcesSection(sources: List<Source>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sources.forEach { source ->
            SourceCard(source)
        }
    }
}

@Composable
fun SourceCard(source: Source) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(source.name)
                Text(
                    source.status,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                if (source.connected) Icons.Default.Cloud else Icons.Default.CloudOff,
                contentDescription = null
            )
        }
    }
}

@Composable
fun MiniPlayer() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("No song playing")

            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun SongsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Songs Screen")
    }
}

@Composable
fun FoldersScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Folders Screen")
    }
}

@Composable
fun PlaylistDetailScreen(id: String?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Playlist ID: $id")
    }
}
