package com.unitn.audioindexer.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    foreignKeys = [
        ForeignKey(
            entity = MusicSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sourceId")]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceId: Int,
    val name: String,
    val coverType: String,
    val coverValue: String,
    val isAlbum: Boolean = false,
    val albumArtistId: Int? = null,
    val artistNameOverride: String? = null,
    val releaseYear: Int? = null
)
