package com.unitn.audioindexer.data.components

data class ArtistUiState(
    val artists: List<Artist>
)

data class AlbumUiState(
    val albums: List<Album>
)

data class PlaylistUiState(
    val playlists: List<Playlist>
)
