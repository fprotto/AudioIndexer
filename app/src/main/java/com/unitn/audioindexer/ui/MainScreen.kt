package com.unitn.audioindexer.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.unitn.audioindexer.data.sampleAlbumsState
import com.unitn.audioindexer.data.sampleArtistsState
import com.unitn.audioindexer.data.samplePlaylistsState

@SuppressLint("RememberReturnType")
@Composable
fun MainScreen(
    navController: NavController,
    sampleState: String, // FIXME: to remove once the data layer is implemented
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = remember {
        when (sampleState) {
            "Albums" -> sampleAlbumsState()
            "Artists" -> sampleArtistsState()
            "Playlists" -> samplePlaylistsState()
        }
    }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { MiniPlayer() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { QuickNavigateToSection(navController = navController) }
            item { SectionTitle(sampleState) }
            item { content() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
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
            Text(stringResource(R.string.no_song_playing))

            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun QuickNavigateToSection(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        QuickChip(stringResource(R.string.artists_header)) {
            navController.navigate("artists")
        }
        QuickChip(stringResource(R.string.albums_header)) {
            navController.navigate("albums")
        }
        QuickChip(stringResource(R.string.playlists_header)) {
            navController.navigate("playlists")
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
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium
    )
}
