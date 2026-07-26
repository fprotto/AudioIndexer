package com.unitn.audioindexer.data.components

data class Song(
    val id: Int,
    val title: String,
    val artist: Artist,
    val artistName: String, // Full artist name for display
    val cover: IconSource,
    val path: String,
    val releaseYear: Int,
    val duration: Long = 0, // Duration in milliseconds
    val playCount: Int = 0,
    val playlistOrder: Int = 0,
    val lyrics: String? = null,
    val isDeleted: Boolean = false
)
