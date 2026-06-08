package com.spotlyric.app.data.repository

import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao
import com.spotlyric.app.data.local.db.dao.SongLyricsDao
import com.spotlyric.app.data.local.db.entity.BookmarkedSongEntity
import com.spotlyric.app.data.local.db.entity.SongLyricsEntity
import com.spotlyric.app.data.remote.extractor.LyricsExtractorService
import com.spotlyric.app.data.remote.gemini.GeminiLyricsService
import com.spotlyric.app.data.remote.gemini.ExtractionResponse
import com.spotlyric.app.domain.error.LyricsResult
import com.spotlyric.app.domain.model.BookmarkedSong
import com.spotlyric.app.domain.repository.ManageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManageRepositoryImpl @Inject constructor(
    private val bookmarkedSongDao: BookmarkedSongDao,
    private val songLyricsDao: SongLyricsDao,
    private val lyricsExtractorService: LyricsExtractorService,
    private val geminiLyricsService: GeminiLyricsService
) : ManageRepository {

    override fun getSongsFlow(): Flow<List<BookmarkedSong>> {
        return bookmarkedSongDao.getAllFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun searchSongsFlow(query: String): Flow<List<BookmarkedSong>> {
        return bookmarkedSongDao.searchFlow(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun deleteSong(id: Long) {
        bookmarkedSongDao.deleteById(id) // CASCADE handles lyrics
    }

    override suspend fun generateAiTranslation(
        bookmarkId: Long,
        sourceUrl: String
    ): Boolean = withContext(Dispatchers.IO) {
        val lyrics = songLyricsDao.findByBookmarkId(bookmarkId)

        val originalLyrics: String
        var extractedData: ExtractionResponse? = null
        if (lyrics == null || lyrics.originalLyrics.isBlank()) {
            // Need to extract lyrics from source URL first
            val html = lyricsExtractorService.fetchPageContent(sourceUrl)
                ?: throw Exception("Failed to download page. Please check your internet connection.")

            if (html.contains("challenge-error-text") ||
                html.contains("Enable JavaScript and cookies to continue") ||
                html.contains("cf-browser-verification") ||
                html.contains("_cf_chl_opt")
            ) {
                throw Exception("This source is protected by Cloudflare bot protection. Please choose another source.")
            }

            val cleanContent = lyricsExtractorService.cleanHtmlForGemini(html)
                ?: throw Exception("Failed to extract readable text from this webpage. Please try another source.")

            // Get bookmark info for song/artist names
            val bookmarkEntity = bookmarkedSongDao.findById(bookmarkId)
            val songName = bookmarkEntity?.songName ?: ""
            val artistName = bookmarkEntity?.artistName ?: ""

            val extractionResult = geminiLyricsService.extractLyricsFromContent(
                cleanContent, songName, artistName
            )
            
            val data = when (extractionResult) {
                is LyricsResult.Success -> extractionResult.data
                is LyricsResult.Error -> 
                    throw Exception(extractionResult.message)
            }
            
            if (!data.success) {
                throw Exception(data.extractionNotes.takeIf { it.isNotBlank() } ?: "Lyrics extraction failed")
            }

            originalLyrics = data.originalLyrics
            if (originalLyrics.isBlank()) {
                throw Exception("No lyrics found")
            }

            songLyricsDao.insert(
                SongLyricsEntity(
                    bookmarkId = bookmarkId,
                    originalLyrics = originalLyrics,
                    translatedLyrics = data.translatedLyrics
                )
            )
            extractedData = data
        } else {
            originalLyrics = lyrics.originalLyrics
        }

        val currentTranslatedLyrics = if (lyrics == null) {
            extractedData?.translatedLyrics ?: ""
        } else {
            lyrics.translatedLyrics ?: ""
        }
        val currentOriginalLanguage = if (lyrics == null) {
            extractedData?.originalLanguage ?: ""
        } else {
            lyrics.originalLanguage ?: ""
        }

        // Generate AI translation and romanization
        val originalIsRomanized = com.spotlyric.app.domain.util.TextUtil.isTextRomanized(originalLyrics)
        val hasSourceTranslation = currentTranslatedLyrics.isNotBlank()
        val needTranslation = !hasSourceTranslation && currentOriginalLanguage != "en"
        val needRomanization = !originalIsRomanized
        val needAi = needTranslation || needRomanization

        var aiRomanized: String? = null
        var aiTranslation: String? = null

        if (needAi) {
            val aiResult = geminiLyricsService.generateAiTranslation(
                originalLyrics, "", ""  // song/artist not strictly needed for translation
            )

            val translationData = when (aiResult) {
                is LyricsResult.Success -> aiResult.data
                is LyricsResult.Error -> 
                    throw Exception(aiResult.message)
            }

            if (!translationData.success) {
                throw Exception(translationData.translationNotes.takeIf { it.isNotBlank() } ?: "AI translation failed")
            }

            if (needRomanization) {
                aiRomanized = translationData.romanizedLyrics
            }
            if (needTranslation) {
                aiTranslation = translationData.wordToWordTranslation
            }
        }

        songLyricsDao.updateAiFields(bookmarkId, aiRomanized, aiTranslation)
        true
    }

    /**
     * Maps Room entity to domain model, enriching with lyrics availability flags.
     */
    private suspend fun BookmarkedSongEntity.toDomainModel(): BookmarkedSong {
        val lyrics = songLyricsDao.findByBookmarkId(id)
        return BookmarkedSong(
            id = id,
            songName = songName,
            artistName = artistName,
            bookmarkedUrl = bookmarkedUrl,
            title = title,
            hasLyrics = lyrics != null && (lyrics.originalLyrics.isNotBlank() || lyrics.translatedLyrics.isNotBlank()),
            hasAiTranslation = !lyrics?.aiTranslation.isNullOrBlank(),
            hasAiRomanized = !lyrics?.aiRomanized.isNullOrBlank()
        )
    }
}
