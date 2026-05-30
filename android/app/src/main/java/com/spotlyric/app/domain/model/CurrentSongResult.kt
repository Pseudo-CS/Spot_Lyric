package com.spotlyric.app.domain.model

sealed interface CurrentSongResult {
    data class Playing(val song: Song) : CurrentSongResult
    data object NothingPlaying : CurrentSongResult
    data class Error(val message: String) : CurrentSongResult
}
