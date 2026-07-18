package com.unitn.audioindexer.data.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.unitn.audioindexer.data.database.entities.ArtistEntity
import com.unitn.audioindexer.data.database.entities.PlaylistEntity
import com.unitn.audioindexer.data.database.entities.PlaylistSongCrossRef
import com.unitn.audioindexer.data.database.entities.SongEntity

data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "albumArtistId",
        entityColumn = "id"
    )
    val albumArtist: ArtistEntity?,
    @Relation(
        entity = SongEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<SongWithArtist>
)
