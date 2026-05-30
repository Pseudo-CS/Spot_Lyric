package com.spotlyric.app.domain.repository

import com.spotlyric.app.domain.model.CurrentSongResult
import com.spotlyric.app.domain.model.Lyrics
import com.spotlyric.app.domain.model.LyricsSource

interface PlayerRepository {
    suspend fun getCurrentSong(token: String): CurrentSongResult
    suspend fun searchLyricsSources(
        songName: String,
        artistName: String,
        preferredDomains: List<String> = emptyList()
    ): List<LyricsSource>
    suspend fun getBookmarkedUrls(songName: String, artistName: String): List<String>
    suspend fun toggleBookmark(
        songName: String,
        artistName: String,
        url: String,
        title: String
    ): Boolean
    suspend fun extractAndTranslateLyrics(
        url: String,
        songName: String,
        artistName: String
    ): Lyrics
    suspend fun getLyrics(songName: String, artistName: String): Lyrics?
    suspend fun generateAiTranslation(
        songName: String,
        artistName: String,
        sourceUrl: String
    ): Lyrics
    suspend fun isBookmarked(songName: String, artistName: String): Boolean
}
