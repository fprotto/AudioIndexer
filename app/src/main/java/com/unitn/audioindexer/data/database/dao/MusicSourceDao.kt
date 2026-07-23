package com.unitn.audioindexer.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.unitn.audioindexer.data.database.entities.MusicSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicSourceDao {
    @Query("SELECT * FROM music_sources")
    fun getAllSources(): Flow<List<MusicSourceEntity>>

    @Query("SELECT COUNT(*) FROM music_sources")
    suspend fun getSourceCount(): Int

    @Query("SELECT * FROM music_sources WHERE id = :id")
    suspend fun getSourceById(id: Int): MusicSourceEntity?

    @Upsert
    suspend fun upsertSource(source: MusicSourceEntity): Long

    @Delete
    suspend fun deleteSource(source: MusicSourceEntity): Int
}
