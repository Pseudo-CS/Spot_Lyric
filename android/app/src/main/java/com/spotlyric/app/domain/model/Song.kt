package com.spotlyric.app.domain.model

data class Song(
    val songName: String,
    val artistName: String,
    val albumArtUrl: String? = null
)
