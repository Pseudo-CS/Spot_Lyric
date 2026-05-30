package com.spotlyric.app.domain.model

data class BookmarkedSong(
    val id: Long,
    val songName: String,
    val artistName: String,
    val bookmarkedUrl: String,
    val title: String,
    val hasLyrics: Boolean = false,
    val hasAiTranslation: Boolean = false,
    val hasAiRomanized: Boolean = false
)
