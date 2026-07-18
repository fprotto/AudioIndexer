package com.unitn.audioindexer.data.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.unitn.audioindexer.data.database.entities.ArtistEntity
import com.unitn.audioindexer.data.database.entities.SongEntity

data class SongWithArtist(
    @Embedded val song: SongEntity,
    @Relation(
        parentColumn = "artistId",
        entityColumn = "id"
    )
    val artist: ArtistEntity
)
