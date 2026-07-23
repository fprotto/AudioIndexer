package com.unitn.audioindexer.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.unitn.audioindexer.data.database.entities.PlaylistEntity
import com.unitn.audioindexer.data.database.entities.PlaylistSongCrossRef
import com.unitn.audioindexer.data.database.relations.PlaylistWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Transaction
    @Query("SELECT * FROM playlists WHERE sourceId = :sourceId AND isAlbum = 0")
    fun getPlaylistsBySource(sourceId: Int): Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE sourceId = :sourceId AND isAlbum = 1")
    fun getAlbumsBySource(sourceId: Int): Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Int): PlaylistWithSongs?

    @Query("SELECT * FROM playlists WHERE name = :name AND sourceId = :sourceId AND isAlbum = :isAlbum")
    suspend fun getPlaylistByName(name: String, sourceId: Int, isAlbum: Boolean): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity): Int

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity): Int

    @Query("DELETE FROM playlists WHERE sourceId = :sourceId AND isAlbum = 1")
    suspend fun deleteAlbumsBySource(sourceId: Int): Int
    
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun deleteSongsFromPlaylist(playlistId: Int): Int

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteSongFromPlaylist(playlistId: Int, songId: Int): Int

    @Query("SELECT MAX(`order`) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getMaxOrderForPlaylist(playlistId: Int): Int?

    @Query("SELECT * FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getCrossRefsForPlaylist(playlistId: Int): List<PlaylistSongCrossRef>
}
