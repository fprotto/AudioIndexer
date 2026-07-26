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
    indices = [
        Index("artistId"),
        Index("sourceId"),
        Index(value = ["path", "sourceId"], unique = true)
    ]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceId: Int,
    val title: String,
    val artistId: Int,
    val releaseYear: Int,
    val artistNameOverride: String? = null,
    val coverType: String = "vector",
    val coverValue: String = "MusicNote",
    val playCount: Int = 0,
    val duration: Long = 0, // Duration in milliseconds
    val source: String, // e.g., "local", "remote_server_1"
    val path: String, // URI or File path
    val lyrics: String? = null,
    val isDeleted: Boolean = false
)
