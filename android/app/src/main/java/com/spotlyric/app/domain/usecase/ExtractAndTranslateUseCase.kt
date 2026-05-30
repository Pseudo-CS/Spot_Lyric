package com.spotlyric.app.domain.usecase

import com.spotlyric.app.domain.model.Lyrics
import com.spotlyric.app.domain.repository.PlayerRepository
import javax.inject.Inject

class ExtractAndTranslateUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(
        url: String,
        songName: String,
        artistName: String
    ): Lyrics {
        return playerRepository.extractAndTranslateLyrics(url, songName, artistName)
    }
}
