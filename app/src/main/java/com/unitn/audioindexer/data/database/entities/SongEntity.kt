package com.unitn.audioindexer.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["id"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MusicSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("artistId"), Index("sourceId")]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceId: Int,
    val title: String,
    val artistId: Int,
    val releaseYear: Int,
    val coverType: String = "vector",
    val coverValue: String = "MusicNote",
    val playCount: Int = 0,
    val source: String, // e.g., "local", "remote_server_1"
    val path: String // URI or File path
)
