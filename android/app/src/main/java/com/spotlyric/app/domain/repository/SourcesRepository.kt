package com.spotlyric.app.domain.repository

import com.spotlyric.app.domain.model.PreferredSource
import kotlinx.coroutines.flow.Flow

interface SourcesRepository {
    fun getAllSourcesFlow(): Flow<List<PreferredSource>>
    suspend fun getEnabledSources(): List<PreferredSource>
    suspend fun addSource(domain: String, displayName: String)
    suspend fun deleteSource(id: Long)
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
