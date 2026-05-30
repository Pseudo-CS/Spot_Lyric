package com.spotlyric.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spotlyric.app.data.local.db.entity.BookmarkedSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkedSongDao {

    @Query("SELECT * FROM bookmarked_song ORDER BY id DESC")
    fun getAllFlow(): Flow<List<BookmarkedSongEntity>>

    @Query(
        """SELECT * FROM bookmarked_song 
           WHERE songName LIKE '%' || :query || '%' 
              OR artistName LIKE '%' || :query || '%' 
           ORDER BY id DESC"""
    )
    fun searchFlow(query: String): Flow<List<BookmarkedSongEntity>>

    @Query(
        """SELECT * FROM bookmarked_song 
           WHERE LOWER(songName) = LOWER(:songName) 
             AND LOWER(artistName) = LOWER(:artistName) 
           LIMIT 1"""
    )
    suspend fun findBySongAndArtist(songName: String, artistName: String): BookmarkedSongEntity?

    @Query("SELECT * FROM bookmarked_song WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): BookmarkedSongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BookmarkedSongEntity): Long

    @Query("DELETE FROM bookmarked_song WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """SELECT bookmarkedUrl FROM bookmarked_song 
           WHERE LOWER(songName) = LOWER(:songName) 
             AND LOWER(artistName) = LOWER(:artistName)"""
    )
    suspend fun getBookmarkedUrls(songName: String, artistName: String): List<String>
}
