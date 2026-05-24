package com.unitn.audioindexer.ui.screens.tracks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.data.sampleSongs
import com.unitn.audioindexer.ui.screens.MainScreen
import com.unitn.audioindexer.ui.songs.SongCard
import kotlin.collections.forEach

@Composable
fun TracksScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    MainScreen(
        navController = navController,
        sampleState = "Tracks"
    ) {
        TracksSection(
            sampleSongs()
        )
    }
}

@Composable
fun TracksSection(
    songs: List<Song>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        songs.forEach { song ->
            SongCard(song)
        }
    }
}
