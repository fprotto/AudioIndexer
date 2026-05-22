package com.unitn.audioindexer.ui.screens.artists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.data.sampleArtistsState
import com.unitn.audioindexer.ui.MainScreen
import kotlin.collections.forEach

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
            sampleArtistsState().artists
        )
    }
}

@Composable
fun ArtistSection(
    artists: List<Artist>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        artists.forEach { artist ->
            ArtistItem(
                artist
            )
        }
    }
}

@Composable
fun ArtistItem(
    artist: Artist
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(artist.name)
        }
    }
}
