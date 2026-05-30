package com.spotlyric.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "preferred_source",
    indices = [Index(value = ["domain"], unique = true)]
)
data class PreferredSourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val domain: String,
    val displayName: String,
    val enabled: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)
