package com.unitn.audioindexer.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val coverType: String,
    val coverValue: String,
    val isAlbum: Boolean = false,
    val albumArtistId: Int? = null,
    val releaseYear: Int? = null
)
