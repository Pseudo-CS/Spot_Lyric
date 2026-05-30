package com.spotlyric.app.data.repository

import com.google.gson.GsonBuilder
import com.spotlyric.app.data.local.backup.BackupBookmark
import com.spotlyric.app.data.local.backup.BackupData
import com.spotlyric.app.data.local.backup.BackupLyrics
import com.spotlyric.app.data.local.backup.BackupPreferredSource
import com.spotlyric.app.data.local.backup.BackupSettings
import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao
import com.spotlyric.app.data.local.db.dao.PreferredSourceDao
import com.spotlyric.app.data.local.db.dao.SongLyricsDao
import com.spotlyric.app.data.local.db.entity.BookmarkedSongEntity
import com.spotlyric.app.data.local.db.entity.PreferredSourceEntity
import com.spotlyric.app.data.local.db.entity.SongLyricsEntity
import com.spotlyric.app.data.local.datastore.SettingsPreferences
import com.spotlyric.app.domain.repository.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val bookmarkedSongDao: BookmarkedSongDao,
    private val songLyricsDao: SongLyricsDao,
    private val preferredSourceDao: PreferredSourceDao,
    private val settingsPreferences: SettingsPreferences
) : BackupRepository {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override suspend fun exportData(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch DB records
            val bookmarkEntities = bookmarkedSongDao.getAllFlow().first()
            val preferredSourceEntities = preferredSourceDao.getAllFlow().first()
            val songLyricsEntities = songLyricsDao.getAll()

            // Map lyrics by bookmark ID
            val lyricsMap = songLyricsEntities.associateBy { it.bookmarkId }

            // Convert to backup structures
            val backupBookmarks = bookmarkEntities.map { bookmark ->
                val lyricsEntity = lyricsMap[bookmark.id]
                val backupLyrics = lyricsEntity?.let {
                    BackupLyrics(
                        originalLyrics = it.originalLyrics,
                        translatedLyrics = it.translatedLyrics,
                        aiRomanized = it.aiRomanized,
                        aiTranslation = it.aiTranslation,
                        sourceUrl = it.sourceUrl,
                        extractionStage = it.extractionStage,
                        confidenceScore = it.confidenceScore,
                        originalLanguage = it.originalLanguage
                    )
                }
                BackupBookmark(
                    songName = bookmark.songName,
                    artistName = bookmark.artistName,
                    bookmarkedUrl = bookmark.bookmarkedUrl,
                    title = bookmark.title,
                    lyrics = backupLyrics
                )
            }

            val backupSources = preferredSourceEntities.map { source ->
                BackupPreferredSource(
                    domain = source.domain,
                    displayName = source.displayName,
                    enabled = source.enabled
                )
            }

            // 2. Fetch current settings
            val backupSettings = BackupSettings(
                relevanceFilterEnabled = settingsPreferences.isRelevanceFilterEnabled.first(),
                relevanceThreshold = settingsPreferences.relevanceThreshold.first(),
                spotifyCustomClientId = settingsPreferences.spotifyCustomClientId.first(),
                serpApiCustomApiKey = settingsPreferences.serpApiCustomApiKey.first(),
                geminiCustomApiKey = settingsPreferences.geminiCustomApiKey.first(),
                geminiCustomModel = settingsPreferences.geminiCustomModel.first()
            )

            // Assemble everything
            val backupData = BackupData(
                version = 1,
                bookmarks = backupBookmarks,
                preferredSources = backupSources,
                settings = backupSettings
            )

            // Write to OutputStream
            OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                gson.toJson(backupData, writer)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importData(inputStream: InputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonContent = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }.trim()
            if (jsonContent.isEmpty()) {
                throw IllegalArgumentException("Backup data is empty")
            }

            val backupData: BackupData
            if (jsonContent.startsWith("[")) {
                // Parse legacy format (JSON array of song/lyrics objects)
                val itemType = object : com.google.gson.reflect.TypeToken<List<OldBackupItem>>() {}.type
                val oldItems: List<OldBackupItem> = gson.fromJson(jsonContent, itemType) ?: emptyList()
                
                val bookmarks = oldItems.mapNotNull { item ->
                    val song = item.song ?: return@mapNotNull null
                    val lyrics = item.lyrics?.let {
                        BackupLyrics(
                            originalLyrics = it.originalLyrics ?: "",
                            translatedLyrics = it.translatedLyrics ?: "",
                            aiRomanized = it.aiRomanized,
                            aiTranslation = it.aiTranslation
                        )
                    }
                    BackupBookmark(
                        songName = song.songName ?: "",
                        artistName = song.artistName ?: "",
                        bookmarkedUrl = song.bookmarkedUrl ?: "",
                        title = song.title ?: "",
                        lyrics = lyrics
                    )
                }
                backupData = BackupData(version = 1, bookmarks = bookmarks)
            } else {
                backupData = gson.fromJson(jsonContent, BackupData::class.java)
                    ?: throw IllegalArgumentException("Invalid backup JSON")
            }

            // 2. Import Preferred Sources
            backupData.preferredSources.forEach { source ->
                preferredSourceDao.insert(
                    PreferredSourceEntity(
                        domain = source.domain,
                        displayName = source.displayName,
                        enabled = source.enabled
                    )
                )
            }

            // 3. Import Settings (only if parsing new object format containing settings)
            if (jsonContent.startsWith("{")) {
                settingsPreferences.setRelevanceFilterEnabled(backupData.settings.relevanceFilterEnabled)
                settingsPreferences.setRelevanceThreshold(backupData.settings.relevanceThreshold)
                settingsPreferences.setSpotifyCustomClientId(backupData.settings.spotifyCustomClientId)
                settingsPreferences.setSerpApiCustomApiKey(backupData.settings.serpApiCustomApiKey)
                settingsPreferences.setGeminiCustomApiKey(backupData.settings.geminiCustomApiKey)
                settingsPreferences.setGeminiCustomModel(backupData.settings.geminiCustomModel)
            }

            // 4. Import Bookmarks & Lyrics
            backupData.bookmarks.forEach { backupBookmark ->
                // Check if a bookmark with the same songName & artistName exists
                val existingBookmark = bookmarkedSongDao.findBySongAndArtist(
                    backupBookmark.songName,
                    backupBookmark.artistName
                )
                if (existingBookmark != null) {
                    // CASCADE delete the existing bookmark and its associated lyrics to start fresh for this song
                    bookmarkedSongDao.deleteById(existingBookmark.id)
                }

                // Insert bookmark
                val newBookmarkId = bookmarkedSongDao.insert(
                    BookmarkedSongEntity(
                        songName = backupBookmark.songName,
                        artistName = backupBookmark.artistName,
                        bookmarkedUrl = backupBookmark.bookmarkedUrl,
                        title = backupBookmark.title
                    )
                )

                // Insert associated lyrics if present
                backupBookmark.lyrics?.let { lyrics ->
                    songLyricsDao.insert(
                        SongLyricsEntity(
                            bookmarkId = newBookmarkId,
                            originalLyrics = lyrics.originalLyrics,
                            translatedLyrics = lyrics.translatedLyrics,
                            aiRomanized = lyrics.aiRomanized,
                            aiTranslation = lyrics.aiTranslation,
                            sourceUrl = lyrics.sourceUrl,
                            extractionStage = lyrics.extractionStage,
                            confidenceScore = lyrics.confidenceScore,
                            originalLanguage = lyrics.originalLanguage
                        )
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private data class OldBackupItem(
    val song: OldSong? = null,
    val lyrics: OldLyrics? = null
)

private data class OldSong(
    val songName: String? = null,
    val artistName: String? = null,
    val bookmarkedUrl: String? = null,
    val title: String? = null
)

private data class OldLyrics(
    val originalLyrics: String? = null,
    val translatedLyrics: String? = null,
    val aiRomanized: String? = null,
    val aiTranslation: String? = null
)

