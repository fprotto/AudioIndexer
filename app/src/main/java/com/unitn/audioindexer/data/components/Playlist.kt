package com.unitn.audioindexer.data.components

data class Playlist(
    val id: Int,
    val name: String,
    val cover: IconSource,
    val songs: List<Song>,
)
