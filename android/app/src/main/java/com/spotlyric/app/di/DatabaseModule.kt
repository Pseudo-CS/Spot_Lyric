package com.spotlyric.app.di

import android.content.Context
import androidx.room.Room
import com.spotlyric.app.data.local.db.SpotLyricDatabase
import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao
import com.spotlyric.app.data.local.db.dao.PreferredSourceDao
import com.spotlyric.app.data.local.db.dao.SongLyricsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SpotLyricDatabase {
        val dbFile = context.getDatabasePath("spotlyric.db")
        val targetVersion = 3 // Current Room database version

        if (dbFile.exists()) {
            try {
                android.database.sqlite.SQLiteDatabase.openDatabase(
                    dbFile.absolutePath,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                ).use { db ->
                    val currentVersion = db.version
                    if (currentVersion in 1 until targetVersion) {
                        performAutoBackupBeforeMigration(context, db, currentVersion)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DatabaseModule", "Failed to check version or perform auto-backup", e)
            }
        }

        return Room.databaseBuilder(
            context,
            SpotLyricDatabase::class.java,
            "spotlyric.db"
        )
        .fallbackToDestructiveMigration()
        .addCallback(object : androidx.room.RoomDatabase.Callback() {
            override fun onDestructiveMigration(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                restoreAutoBackupAfterMigration(context, db)
            }
        })
        .build()
    }

    private fun performAutoBackupBeforeMigration(
        context: Context,
        db: android.database.sqlite.SQLiteDatabase,
        version: Int
    ) {
        try {
            val backupFile = java.io.File(context.filesDir, "migration_backup_v$version.json")
            if (backupFile.exists()) {
                backupFile.delete()
            }

            // 1. Read bookmarked_song
            val bookmarks = mutableListOf<com.spotlyric.app.data.local.backup.BackupBookmark>()
            db.rawQuery("SELECT * FROM bookmarked_song", null).use { cursor ->
                val idCol = cursor.getColumnIndex("id")
                val songCol = cursor.getColumnIndex("songName")
                val artistCol = cursor.getColumnIndex("artistName")
                val urlCol = cursor.getColumnIndex("bookmarkedUrl")
                val titleCol = cursor.getColumnIndex("title")

                while (cursor.moveToNext()) {
                    val id = if (idCol >= 0) cursor.getLong(idCol) else -1L
                    val songName = if (songCol >= 0) cursor.getString(songCol) else ""
                    val artistName = if (artistCol >= 0) cursor.getString(artistCol) else ""
                    val url = if (urlCol >= 0) cursor.getString(urlCol) else ""
                    val title = if (titleCol >= 0) cursor.getString(titleCol) else ""

                    // Get lyrics for this bookmark
                    var lyrics: com.spotlyric.app.data.local.backup.BackupLyrics? = null
                    if (id != -1L) {
                        db.rawQuery("SELECT * FROM song_lyrics WHERE bookmarkId = ?", arrayOf(id.toString())).use { lCursor ->
                            if (lCursor.moveToFirst()) {
                                val origCol = lCursor.getColumnIndex("originalLyrics")
                                val transCol = lCursor.getColumnIndex("translatedLyrics")
                                val romCol = lCursor.getColumnIndex("aiRomanized")
                                val aiTransCol = lCursor.getColumnIndex("aiTranslation")
                                val srcCol = lCursor.getColumnIndex("sourceUrl")
                                val stageCol = lCursor.getColumnIndex("extractionStage")
                                val confCol = lCursor.getColumnIndex("confidenceScore")
                                val langCol = lCursor.getColumnIndex("originalLanguage")

                                lyrics = com.spotlyric.app.data.local.backup.BackupLyrics(
                                    originalLyrics = if (origCol >= 0) lCursor.getString(origCol) else "",
                                    translatedLyrics = if (transCol >= 0) lCursor.getString(transCol) else "",
                                    aiRomanized = if (romCol >= 0) lCursor.getString(romCol) else null,
                                    aiTranslation = if (aiTransCol >= 0) lCursor.getString(aiTransCol) else null,
                                    sourceUrl = if (srcCol >= 0) lCursor.getString(srcCol) else null,
                                    extractionStage = if (stageCol >= 0) lCursor.getString(stageCol) else null,
                                    confidenceScore = if (confCol >= 0 && !lCursor.isNull(confCol)) lCursor.getFloat(confCol) else null,
                                    originalLanguage = if (langCol >= 0) lCursor.getString(langCol) else null
                                )
                            }
                        }
                    }

                    bookmarks.add(
                        com.spotlyric.app.data.local.backup.BackupBookmark(
                            songName = songName,
                            artistName = artistName,
                            bookmarkedUrl = url,
                            title = title,
                            lyrics = lyrics
                        )
                    )
                }
            }

            // 2. Read preferred_source
            val preferredSources = mutableListOf<com.spotlyric.app.data.local.backup.BackupPreferredSource>()
            try {
                db.rawQuery("SELECT * FROM preferred_source", null).use { cursor ->
                    val domCol = cursor.getColumnIndex("domain")
                    val dispCol = cursor.getColumnIndex("displayName")
                    val enCol = cursor.getColumnIndex("enabled")

                    while (cursor.moveToNext()) {
                        val domain = if (domCol >= 0) cursor.getString(domCol) else ""
                        val displayName = if (dispCol >= 0) cursor.getString(dispCol) else ""
                        val enabled = if (enCol >= 0) cursor.getInt(enCol) != 0 else true

                        preferredSources.add(
                            com.spotlyric.app.data.local.backup.BackupPreferredSource(
                                domain = domain,
                                displayName = displayName,
                                enabled = enabled
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Table might not exist in older schema version
            }

            val backupData = com.spotlyric.app.data.local.backup.BackupData(
                version = 1,
                bookmarks = bookmarks,
                preferredSources = preferredSources,
                settings = com.spotlyric.app.data.local.backup.BackupSettings()
            )

            val gson = com.google.gson.Gson()
            backupFile.bufferedWriter(java.nio.charset.StandardCharsets.UTF_8).use { writer ->
                gson.toJson(backupData, writer)
            }
            android.util.Log.i("DatabaseModule", "Auto-backup created successfully before migration: ${backupFile.absolutePath}")
        } catch (e: Exception) {
            android.util.Log.e("DatabaseModule", "Auto-backup failed before migration", e)
        }
    }

    private fun restoreAutoBackupAfterMigration(
        context: Context,
        db: androidx.sqlite.db.SupportSQLiteDatabase
    ) {
        try {
            // Find migration backup files
            val backupFiles = context.filesDir.listFiles { _, name ->
                name.startsWith("migration_backup_v") && name.endsWith(".json")
            }
            if (backupFiles.isNullOrEmpty()) {
                android.util.Log.i("DatabaseModule", "No auto-backup file found to restore")
                return
            }

            // Take the most recent/any backup file
            val backupFile = backupFiles.maxByOrNull { it.lastModified() } ?: return
            
            android.util.Log.i("DatabaseModule", "Restoring auto-backup file: ${backupFile.absolutePath}")
            val jsonContent = backupFile.bufferedReader(java.nio.charset.StandardCharsets.UTF_8).use { it.readText() }
            val gson = com.google.gson.Gson()
            val backupData = gson.fromJson(jsonContent, com.spotlyric.app.data.local.backup.BackupData::class.java)

            if (backupData != null) {
                // 1. Restore Preferred Sources
                backupData.preferredSources.forEach { source ->
                    val cv = android.content.ContentValues().apply {
                        put("domain", source.domain)
                        put("displayName", source.displayName)
                        put("enabled", if (source.enabled) 1 else 0)
                        put("addedAt", System.currentTimeMillis())
                    }
                    db.insert("preferred_source", android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, cv)
                }

                // 2. Restore Bookmarks & Lyrics
                backupData.bookmarks.forEach { bookmark ->
                    val bookmarkCv = android.content.ContentValues().apply {
                        put("songName", bookmark.songName)
                        put("artistName", bookmark.artistName)
                        put("bookmarkedUrl", bookmark.bookmarkedUrl)
                        put("title", bookmark.title)
                    }
                    val newBookmarkId = db.insert("bookmarked_song", android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, bookmarkCv)

                    bookmark.lyrics?.let { lyrics ->
                        val lyricsCv = android.content.ContentValues().apply {
                            put("bookmarkId", newBookmarkId)
                            put("originalLyrics", lyrics.originalLyrics)
                            put("translatedLyrics", lyrics.translatedLyrics)
                            put("aiRomanized", lyrics.aiRomanized)
                            put("aiTranslation", lyrics.aiTranslation)
                            put("sourceUrl", lyrics.sourceUrl)
                            put("extractionStage", lyrics.extractionStage)
                            put("confidenceScore", lyrics.confidenceScore)
                            put("originalLanguage", lyrics.originalLanguage)
                        }
                        db.insert("song_lyrics", android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, lyricsCv)
                    }
                }
                android.util.Log.i("DatabaseModule", "Auto-backup restored successfully!")
            }
            
            // Clean up files
            backupFiles.forEach { it.delete() }
        } catch (e: Exception) {
            android.util.Log.e("DatabaseModule", "Auto-restore failed after destructive migration", e)
        }
    }

    @Provides
    fun provideBookmarkedSongDao(db: SpotLyricDatabase): BookmarkedSongDao {
        return db.bookmarkedSongDao()
    }

    @Provides
    fun provideSongLyricsDao(db: SpotLyricDatabase): SongLyricsDao {
        return db.songLyricsDao()
    }

    @Provides
    fun providePreferredSourceDao(db: SpotLyricDatabase): PreferredSourceDao {
        return db.preferredSourceDao()
    }
}
