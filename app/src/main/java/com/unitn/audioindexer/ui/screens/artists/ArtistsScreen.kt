package com.unitn.audioindexer.ui.screens.artists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.data.sampleArtistsState
import com.unitn.audioindexer.ui.screens.MainScreen
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
            sampleArtistsState().artists,
            navController
        )
    }
}

@Composable
fun ArtistSection(
    artists: List<Artist>,
    navController: NavController
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        artists.forEach { artist ->
            ArtistItem(
                artist,
                navController
            )
        }
    }
}

@Composable
fun ArtistItem(
    artist: Artist,
    navController: NavController
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
fun ArtistDetailScreen(id: String?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Artist ID: $id")
    }
}
