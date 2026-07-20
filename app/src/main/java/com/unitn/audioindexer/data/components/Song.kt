package com.unitn.audioindexer.data.components

data class Song(
    val id: Int,
    val title: String,
    val artist: Artist,
    val cover: IconSource,
    val path: String,
    val releaseYear: Int,
    val playCount: Int = 0,
    val playlistOrder: Int = 0
)
