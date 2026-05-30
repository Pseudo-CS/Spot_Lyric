package com.spotlyric.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarked_song",
    indices = [Index(value = ["songName", "artistName"], unique = true)]
)
data class BookmarkedSongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songName: String,
    val artistName: String,
    val bookmarkedUrl: String,
    val title: String
)
