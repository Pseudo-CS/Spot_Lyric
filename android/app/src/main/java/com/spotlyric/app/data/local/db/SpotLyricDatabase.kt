package com.spotlyric.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao
import com.spotlyric.app.data.local.db.dao.PreferredSourceDao
import com.spotlyric.app.data.local.db.dao.SongLyricsDao
import com.spotlyric.app.data.local.db.entity.BookmarkedSongEntity
import com.spotlyric.app.data.local.db.entity.PreferredSourceEntity
import com.spotlyric.app.data.local.db.entity.SongLyricsEntity

@Database(
    entities = [BookmarkedSongEntity::class, SongLyricsEntity::class, PreferredSourceEntity::class],
    version = 3,
    exportSchema = false
)
abstract class SpotLyricDatabase : RoomDatabase() {
    abstract fun bookmarkedSongDao(): BookmarkedSongDao
    abstract fun songLyricsDao(): SongLyricsDao
    abstract fun preferredSourceDao(): PreferredSourceDao
}
