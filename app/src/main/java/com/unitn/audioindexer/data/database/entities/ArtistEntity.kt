package com.unitn.audioindexer.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artists",
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
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceId: Int,
    val name: String,
    val propicType: String, // "vector" or "uri"
    val propicValue: String // Icon name or URI string
)
