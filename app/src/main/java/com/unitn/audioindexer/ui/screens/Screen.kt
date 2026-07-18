package com.unitn.audioindexer.ui.screens

sealed class Screen(val route: String) {
    object Albums : Screen("albums")
    object Album : Screen("album/{albumId}") {
        fun createRoute(id: Int) = "album/$id"
    }

    object Artists : Screen("artists")
    object Artist : Screen("artist/{artistId}") {
        fun createRoute(id: Int) = "artist/$id"
    }

    object Playlists : Screen("playlists")
    object Playlist : Screen("playlist/{playlistId}") {
        fun createRoute(id: Int) = "playlist/$id"
    }

    object Tracks : Screen("tracks")
    object Player : Screen("player")
    object Setup : Screen("setup")
}
