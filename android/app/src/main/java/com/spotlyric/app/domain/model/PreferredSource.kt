package com.spotlyric.app.domain.model

data class PreferredSource(
    val id: Long,
    val domain: String,
    val displayName: String,
    val enabled: Boolean,
    val addedAt: Long
)
