package com.spotlyric.app.domain.repository

import com.spotlyric.app.domain.model.BookmarkedSong
import kotlinx.coroutines.flow.Flow

interface ManageRepository {
    fun getSongsFlow(): Flow<List<BookmarkedSong>>
    fun searchSongsFlow(query: String): Flow<List<BookmarkedSong>>
    suspend fun deleteSong(id: Long)
    suspend fun generateAiTranslation(bookmarkId: Long, sourceUrl: String): Boolean
}
