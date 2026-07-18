package com.unitn.audioindexer.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val propicType: String, // "vector" or "uri"
    val propicValue: String // Icon name or URI string
)
