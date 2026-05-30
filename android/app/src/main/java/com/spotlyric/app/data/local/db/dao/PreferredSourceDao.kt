package com.spotlyric.app.data.local.db.dao

import androidx.room.*
import com.spotlyric.app.data.local.db.entity.PreferredSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferredSourceDao {
    @Query("SELECT * FROM preferred_source ORDER BY addedAt DESC")
    fun getAllFlow(): Flow<List<PreferredSourceEntity>>

    @Query("SELECT * FROM preferred_source WHERE enabled = 1 ORDER BY addedAt DESC")
    suspend fun getEnabled(): List<PreferredSourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(source: PreferredSourceEntity): Long

    @Query("DELETE FROM preferred_source WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE preferred_source SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
