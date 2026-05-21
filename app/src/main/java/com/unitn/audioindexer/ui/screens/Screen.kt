package com.unitn.audioindexer.ui.screens

sealed class Screen(val route: String) {
    object Albums : Screen("albums")
    object Artists : Screen("artists")
    object Playlists : Screen("playlists")
    object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        fun createRoute(id: Int) = "playlist_detail/$id"
    }
}
