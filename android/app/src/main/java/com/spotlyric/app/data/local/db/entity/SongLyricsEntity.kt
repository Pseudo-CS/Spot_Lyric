package com.spotlyric.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "song_lyrics",
    foreignKeys = [
        ForeignKey(
            entity = BookmarkedSongEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookmarkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookmarkId")]
)
data class SongLyricsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookmarkId: Long,
    val originalLyrics: String,
    val translatedLyrics: String,
    val aiRomanized: String? = null,
    val aiTranslation: String? = null,
    val sourceUrl: String? = null,
    val extractionStage: String? = null,
    val confidenceScore: Float? = null,
    val originalLanguage: String? = null
)
