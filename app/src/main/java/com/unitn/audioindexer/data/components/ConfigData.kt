package com.unitn.audioindexer.data.components

data class SourceConfig(
    val type: String,
    val path: String,
    val port: Int?,
    val name: String
)

data class SongConfig(
    val title: String,
    val artistName: String,
    val path: String
)

data class PlaylistConfig(
    val name: String,
    val songs: List<SongConfig>
)

data class ExportConfig(
    val source: SourceConfig,
    val playlists: List<PlaylistConfig>
)
