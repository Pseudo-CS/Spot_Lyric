package com.spotlyric.app.data.repository

import com.spotlyric.app.data.local.datastore.SettingsPreferences
import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao
import com.spotlyric.app.data.local.db.dao.SongLyricsDao
import com.spotlyric.app.data.local.db.entity.BookmarkedSongEntity
import com.spotlyric.app.data.local.db.entity.SongLyricsEntity
import com.spotlyric.app.data.remote.extractor.LyricsExtractorService
import com.spotlyric.app.data.remote.gemini.GeminiLyricsService
import com.spotlyric.app.data.remote.serpapi.LyricsSearchService
import com.spotlyric.app.data.remote.spotify.SpotifyApiService
import com.spotlyric.app.domain.error.LyricsResult
import com.spotlyric.app.domain.model.CurrentSongResult
import com.spotlyric.app.domain.model.Lyrics
import com.spotlyric.app.domain.model.LyricsSource
import com.spotlyric.app.domain.model.Song
import com.spotlyric.app.domain.repository.PlayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val spotifyApiService: SpotifyApiService,
    private val lyricsSearchService: LyricsSearchService,
    private val lyricsExtractorService: LyricsExtractorService,
    private val geminiLyricsService: GeminiLyricsService,
    private val bookmarkedSongDao: BookmarkedSongDao,
    private val songLyricsDao: SongLyricsDao,
    private val settingsPreferences: SettingsPreferences
) : PlayerRepository {

    override suspend fun getCurrentSong(token: String): CurrentSongResult {
        return try {
            val response = spotifyApiService.getCurrentlyPlaying("Bearer $token")
            settingsPreferences.incrementSpotifyRequests()
            when {
                response.code() == 204 || response.body() == null -> {
                    CurrentSongResult.NothingPlaying
                }
                response.code() == 401 -> {
                    CurrentSongResult.Error("Token expired")
                }
                response.isSuccessful -> {
                    val dto = response.body()!!
                    val track = dto.item ?: return CurrentSongResult.NothingPlaying
                    val songName = track.name ?: return CurrentSongResult.NothingPlaying
                    val artistName = track.artists?.firstOrNull()?.name ?: "Unknown Artist"
                    val albumArtUrl = track.album?.images?.firstOrNull()?.url

                    CurrentSongResult.Playing(
                        Song(
                            songName = songName,
                            artistName = artistName,
                            albumArtUrl = albumArtUrl
                        )
                    )
                }
                else -> {
                    CurrentSongResult.Error("Spotify API error: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            CurrentSongResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun searchLyricsSources(
        songName: String,
        artistName: String,
        preferredDomains: List<String>
    ): List<LyricsSource> {
        return lyricsSearchService.searchLyricsSources(songName, artistName, preferredDomains)
    }

    override suspend fun getBookmarkedUrls(
        songName: String,
        artistName: String
    ): List<String> {
        return bookmarkedSongDao.getBookmarkedUrls(songName, artistName)
    }

    override suspend fun toggleBookmark(
        songName: String,
        artistName: String,
        url: String,
        title: String
    ): Boolean {
        val existing = bookmarkedSongDao.findBySongAndArtist(songName, artistName)
        return if (existing != null && existing.bookmarkedUrl == url) {
            // Unbookmark — delete the bookmark (CASCADE deletes lyrics too)
            bookmarkedSongDao.deleteById(existing.id)
            false
        } else {
            // Bookmark — insert/replace
            bookmarkedSongDao.insert(
                BookmarkedSongEntity(
                    songName = songName,
                    artistName = artistName,
                    bookmarkedUrl = url,
                    title = title
                )
            )
            true
        }
    }

    /**
     * Full pipeline: fetch URL → multi-stage extraction → conditional AI translation → save to Room.
     *
     * Extraction stages (tried in order):
     *   1. Domain Parser  — direct Jsoup selectors for Genius / LyricsRaag.
     *   2. Heuristics     — CSS-class keyword scan (.lyrics, .lyric-content, etc.).
     *   3. Gemini (AI)    — clean HTML sent to Gemini for extraction.
     *
     * AI translation is skipped when the extracted content already carries a translation
     * (e.g. LyricsRaag bilingual pages) or when the original language is detected as English.
     * Long songs (>80 lines) are translated in 40-line chunks.
     */
    override suspend fun extractAndTranslateLyrics(
        url: String,
        songName: String,
        artistName: String
    ): Lyrics = withContext(Dispatchers.IO) {
        // Step 1: Fetch HTML
        val html = lyricsExtractorService.fetchPageContent(url)
            ?: throw Exception("Failed to download page. Please check your internet connection.")

        if (html.contains("challenge-error-text") ||
            html.contains("Enable JavaScript and cookies to continue") ||
            html.contains("cf-browser-verification") ||
            html.contains("_cf_chl_opt")
        ) {
            throw Exception("This source is protected by Cloudflare bot protection. Please choose another source from the list.")
        }

        // Step 2: Multi-stage extraction - Only AI parsing is active
        val originalLyrics: String
        val translatedLyrics: String
        val extractionStage: String
        val confidence: Float
        var originalLanguage: String = ""

        // Stage 3: Gemini fallback (Domain parser and heuristics bypassed)
        val cleanContent = lyricsExtractorService.cleanHtmlForGemini(html)
            ?: throw Exception("Failed to extract readable text from this webpage. Please try another source.")

        val geminiResult = geminiLyricsService.extractLyricsFromContent(cleanContent, songName, artistName)
        val extractedData = when (geminiResult) {
            is LyricsResult.Success -> geminiResult.data
            is LyricsResult.Error -> throw Exception(geminiResult.message)
        }

        if (!extractedData.success) {
            throw Exception(extractedData.extractionNotes.takeIf { it.isNotBlank() } ?: "Lyrics extraction failed")
        }
        if (extractedData.originalLyrics.isBlank()) {
            throw Exception("No lyrics found in the extracted content")
        }

        originalLyrics = extractedData.originalLyrics
        translatedLyrics = extractedData.translatedLyrics
        extractionStage = "AI"
        confidence = extractedData.confidenceScore
        originalLanguage = extractedData.originalLanguage

        if (originalLyrics.isBlank()) {
            throw Exception("No lyrics found in the extracted content")
        }

        // Step 3: Conditional AI translation — skip if already translated or original is English
        val skipAiTranslation = translatedLyrics.isNotBlank() || originalLanguage == "en"
        android.util.Log.d("PlayerRepository", "skipAiTranslation=$skipAiTranslation (translatedBlank=${translatedLyrics.isBlank()}, lang=$originalLanguage)")

        val aiResult = if (!skipAiTranslation) {
            geminiLyricsService.generateAiTranslationChunked(originalLyrics, songName, artistName)
        } else null

        // Step 4: Ensure bookmark exists
        val bookmark = bookmarkedSongDao.findBySongAndArtist(songName, artistName)
            ?: run {
                bookmarkedSongDao.insert(
                    BookmarkedSongEntity(
                        songName = songName,
                        artistName = artistName,
                        bookmarkedUrl = url,
                        title = songName
                    )
                )
                bookmarkedSongDao.findBySongAndArtist(songName, artistName)!!
            }

        // Step 5: Replace any existing lyrics row for this bookmark, then insert
        songLyricsDao.deleteByBookmarkId(bookmark.id)

        val (aiRomanized, aiTranslation) = when (aiResult) {
            is LyricsResult.Success -> aiResult.data.romanizedLyrics to aiResult.data.wordToWordTranslation
            is LyricsResult.Error -> {
                android.util.Log.w("PlayerRepository", "AI translation failed: ${aiResult.message}")
                null to null
            }
            null -> null to null
        }

        val entity = SongLyricsEntity(
            bookmarkId = bookmark.id,
            originalLyrics = originalLyrics,
            translatedLyrics = translatedLyrics,
            aiRomanized = aiRomanized,
            aiTranslation = aiTranslation,
            sourceUrl = url,
            extractionStage = extractionStage,
            confidenceScore = confidence,
            originalLanguage = originalLanguage.ifBlank { null }
        )
        val lyricsId = songLyricsDao.insert(entity)

        Lyrics(
            id = lyricsId,
            bookmarkId = bookmark.id,
            originalLyrics = originalLyrics,
            translatedLyrics = translatedLyrics,
            aiRomanized = aiRomanized,
            aiTranslation = aiTranslation,
            sourceUrl = url,
            extractionStage = extractionStage,
            confidenceScore = confidence,
            originalLanguage = originalLanguage.ifBlank { null }
        )
    }

    override suspend fun getLyrics(songName: String, artistName: String): Lyrics? {
        val entity = songLyricsDao.findBySongAndArtist(songName, artistName) ?: return null
        return Lyrics(
            id = entity.id,
            bookmarkId = entity.bookmarkId,
            originalLyrics = entity.originalLyrics,
            translatedLyrics = entity.translatedLyrics,
            aiRomanized = entity.aiRomanized,
            aiTranslation = entity.aiTranslation,
            sourceUrl = entity.sourceUrl,
            extractionStage = entity.extractionStage,
            confidenceScore = entity.confidenceScore,
            originalLanguage = entity.originalLanguage
        )
    }

    override suspend fun generateAiTranslation(
        songName: String,
        artistName: String,
        sourceUrl: String
    ): Lyrics = withContext(Dispatchers.IO) {
        // Check if lyrics already exist
        var lyrics = songLyricsDao.findBySongAndArtist(songName, artistName)

        if (lyrics == null || lyrics.originalLyrics.isBlank()) {
            // Need to extract lyrics first from the source URL
            val cleanContent = lyricsExtractorService.extractAndClean(sourceUrl)
                ?: throw Exception("Failed to fetch content from URL")

            val extractionResult = geminiLyricsService.extractLyricsFromContent(
                cleanContent, songName, artistName
            )
            val extractedData = when (extractionResult) {
                is LyricsResult.Success -> extractionResult.data
                is LyricsResult.Error -> 
                    throw Exception(extractionResult.message)
            }
            if (!extractedData.success) {
                val errorMsg = extractedData.extractionNotes.takeIf { it.isNotBlank() } 
                    ?: "Lyrics extraction failed"
                throw Exception(errorMsg)
            }

            val originalLyrics = extractedData.originalLyrics
            if (originalLyrics.isBlank()) {
                throw Exception("No lyrics found in the extracted content")
            }

            // Save extracted lyrics
            val bookmark = bookmarkedSongDao.findBySongAndArtist(songName, artistName)
                ?: run {
                    bookmarkedSongDao.insert(
                        BookmarkedSongEntity(
                            songName = songName,
                            artistName = artistName,
                            bookmarkedUrl = sourceUrl,
                            title = songName
                        )
                    )
                    bookmarkedSongDao.findBySongAndArtist(songName, artistName)!!
                }

            val translatedLyrics = extractedData.translatedLyrics
            songLyricsDao.insert(
                SongLyricsEntity(
                    bookmarkId = bookmark.id,
                    originalLyrics = originalLyrics,
                    translatedLyrics = translatedLyrics
                )
            )
            lyrics = songLyricsDao.findBySongAndArtist(songName, artistName)!!
        }

        // Generate AI translation
        val aiResult = geminiLyricsService.generateAiTranslation(
            lyrics.originalLyrics, songName, artistName
        )

        val translationData = when (aiResult) {
            is LyricsResult.Success -> aiResult.data
            is LyricsResult.Error -> 
                throw Exception(aiResult.message)
        }

        if (!translationData.success) {
            throw Exception("AI translation failed")
        }

        val aiRomanized = translationData.romanizedLyrics
        val aiTranslation = translationData.wordToWordTranslation

        songLyricsDao.updateAiFields(lyrics.bookmarkId, aiRomanized, aiTranslation)

        Lyrics(
            id = lyrics.id,
            bookmarkId = lyrics.bookmarkId,
            originalLyrics = lyrics.originalLyrics,
            translatedLyrics = lyrics.translatedLyrics,
            aiRomanized = aiRomanized,
            aiTranslation = aiTranslation
        )
    }

    override suspend fun isBookmarked(songName: String, artistName: String): Boolean {
        return bookmarkedSongDao.findBySongAndArtist(songName, artistName) != null
    }
}
