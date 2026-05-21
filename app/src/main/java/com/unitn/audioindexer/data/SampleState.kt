package com.unitn.audioindexer.data

import com.unitn.audioindexer.ui.components.HomeUiState
import com.unitn.audioindexer.ui.components.Playlist
import com.unitn.audioindexer.ui.components.Song
import com.unitn.audioindexer.ui.components.Source

fun sampleHomeState() = HomeUiState(
    recentSongs = listOf(
        Song("Song A", "Artist 1"),
        Song("Song B", "Artist 2"),
        Song("Song C", "Artist 3")
    ),
    playlists = listOf(
        Playlist(1, "Favorites", 25),
        Playlist(2, "Workout", 40),
        Playlist(3, "Workout", 40),
        Playlist(4, "Workout", 40),
        Playlist(5, "Workout", 40),
        Playlist(6, "Workout", 40),
        Playlist(7, "Workout", 40),
        Playlist(8, "Workout", 40)
    ),
    sources = listOf(
        Source("Local", "120 songs", true),
        Source("Remote Server", "Connected", true)
    )
)
