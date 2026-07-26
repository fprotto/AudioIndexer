package com.unitn.audioindexer.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.unitn.audioindexer.data.database.entities.SongEntity
import com.unitn.audioindexer.data.database.relations.SongWithArtist
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Transaction
    @Query("SELECT * FROM songs WHERE sourceId = :sourceId AND isDeleted = 0")
    fun getSongsBySource(sourceId: Int): Flow<List<SongWithArtist>>

    @Transaction
    @Query("SELECT * FROM songs WHERE id = :id AND isDeleted = 0")
    suspend fun getSongById(id: Int): SongWithArtist?

    @Query("SELECT * FROM songs WHERE path = :path AND sourceId = :sourceId")
    suspend fun getSongByPath(path: String, sourceId: Int): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity): Long

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE id = :songId")
    suspend fun incrementPlayCount(songId: Int)

    @Update
    suspend fun updateSong(song: SongEntity): Int

    @Query("UPDATE songs SET lyrics = :lyrics WHERE id = :songId")
    suspend fun updateLyrics(songId: Int, lyrics: String?)

    @Delete
    suspend fun deleteSong(song: SongEntity): Int

    @Query("UPDATE songs SET isDeleted = 1 WHERE id = :songId")
    suspend fun markSongAsDeleted(songId: Int)
}
