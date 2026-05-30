package com.spotlyric.app.domain.usecase

import com.spotlyric.app.domain.repository.PlayerRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    /**
     * @return true if the URL is now bookmarked, false if unbookmarked
     */
    suspend operator fun invoke(
        songName: String,
        artistName: String,
        url: String,
        title: String
    ): Boolean {
        return playerRepository.toggleBookmark(songName, artistName, url, title)
    }
}
