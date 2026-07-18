package com.unitn.audioindexer.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_sources")
data class MusicSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "LOCAL" or "REMOTE"
    val path: String, // URI for local, IP for remote
    val port: Int? = null,
    val name: String
)
