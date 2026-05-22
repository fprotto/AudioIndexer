package com.unitn.audioindexer.ui.screens

sealed class Screen(val route: String) {
    object Albums : Screen("albums")
    object Artists : Screen("artists")
    object Playlists : Screen("playlists")
    object Playlist : Screen("playlist/{playlistId}") {
        fun createRoute(id: Int) = "playlist/$id"
    }
}
