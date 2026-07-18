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
    @Query("SELECT * FROM songs")
    fun getAllSongsWithArtist(): Flow<List<SongWithArtist>>

    @Transaction
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Int): SongWithArtist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity): Long

    @Update
    suspend fun updateSong(song: SongEntity): Int

    @Delete
    suspend fun deleteSong(song: SongEntity): Int
}
