package com.spotlyric.app.domain.model

data class LyricsSource(
    val title: String,
    val url: String,
    val snippet: String? = null,
    val isPreferred: Boolean = false
)
