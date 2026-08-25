package com.unitn.audioindexer.ui.screens.artists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.ui.screens.mainscreen.MainScreen
import com.unitn.audioindexer.ui.viewmodels.ArtistsViewModel
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory

@Composable
fun ArtistsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as AudioIndexerApplication
    val viewModel: ArtistsViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController, app.settingsRepository))

    var searchQuery by remember { mutableStateOf("") }
    val allArtists by viewModel.artists.collectAsState()

    val filteredArtists = remember(searchQuery, allArtists) {
        allArtists.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    MainScreen(
        navController = navController,
        state = "Artists",
        modifier = modifier
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ArtistsControlBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(
            items = filteredArtists,
            key = { artist -> artist.id }
        ) { artist ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                ArtistItem(
                    artist,
                    navController
                )
            }
        }
    }
}
