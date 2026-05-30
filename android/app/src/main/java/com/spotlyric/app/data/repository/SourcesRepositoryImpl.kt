package com.spotlyric.app.data.repository

import com.spotlyric.app.data.local.db.dao.PreferredSourceDao
import com.spotlyric.app.data.local.db.entity.PreferredSourceEntity
import com.spotlyric.app.domain.model.PreferredSource
import com.spotlyric.app.domain.repository.SourcesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourcesRepositoryImpl @Inject constructor(
    private val dao: PreferredSourceDao
) : SourcesRepository {

    override fun getAllSourcesFlow(): Flow<List<PreferredSource>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun getEnabledSources(): List<PreferredSource> =
        dao.getEnabled().map { it.toDomain() }

    override suspend fun addSource(domain: String, displayName: String) {
        dao.insert(
            PreferredSourceEntity(
                domain = domain.lowercase().trim(),
                displayName = displayName.trim().ifBlank { domain }
            )
        )
    }

    override suspend fun deleteSource(id: Long) = dao.deleteById(id)

    override suspend fun setEnabled(id: Long, enabled: Boolean) = dao.setEnabled(id, enabled)

    private fun PreferredSourceEntity.toDomain() = PreferredSource(
        id = id,
        domain = domain,
        displayName = displayName,
        enabled = enabled,
        addedAt = addedAt
    )
}
