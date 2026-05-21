package com.unitn.audioindexer.ui.components

data class HomeUiState(
    val recentSongs: List<Song>,
    val playlists: List<Playlist>,
    val sources: List<Source>
)
