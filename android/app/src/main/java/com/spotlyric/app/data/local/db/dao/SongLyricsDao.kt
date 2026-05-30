package com.spotlyric.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.spotlyric.app.data.local.db.entity.SongLyricsEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface SongLyricsDao {

    @Query("SELECT * FROM song_lyrics WHERE bookmarkId = :bookmarkId LIMIT 1")
    suspend fun findByBookmarkId(bookmarkId: Long): SongLyricsEntity?

    @Query(
        """SELECT sl.* FROM song_lyrics sl 
           INNER JOIN bookmarked_song bs ON sl.bookmarkId = bs.id 
           WHERE LOWER(bs.songName) = LOWER(:songName) 
             AND LOWER(bs.artistName) = LOWER(:artistName) 
           LIMIT 1"""
    )
    suspend fun findBySongAndArtist(songName: String, artistName: String): SongLyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SongLyricsEntity): Long

    @Query(
        """UPDATE song_lyrics
           SET aiRomanized = :aiRomanized, aiTranslation = :aiTranslation
           WHERE bookmarkId = :bookmarkId"""
    )
    suspend fun updateAiFields(bookmarkId: Long, aiRomanized: String?, aiTranslation: String?)

    @Query("DELETE FROM song_lyrics WHERE bookmarkId = :bookmarkId")
    suspend fun deleteByBookmarkId(bookmarkId: Long)

    @Query("SELECT * FROM song_lyrics")
    suspend fun getAll(): List<SongLyricsEntity>

    @Query("SELECT * FROM song_lyrics")
    fun getAllFlow(): Flow<List<SongLyricsEntity>>
}
