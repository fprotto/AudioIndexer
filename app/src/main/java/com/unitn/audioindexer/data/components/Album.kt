package com.unitn.audioindexer.data.components

class Album(
    val id: Int,
    val artist: Artist,
    val artistName: String, // Full artist name for display
    val name: String,
    val cover: IconSource,
    val releaseYear: Int,
    val songs: List<Song>
)