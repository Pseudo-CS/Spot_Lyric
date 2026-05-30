package com.spotlyric.app.domain.usecase

import com.spotlyric.app.domain.model.Lyrics
import com.spotlyric.app.domain.repository.PlayerRepository
import javax.inject.Inject

class AiTranslateUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(
        songName: String,
        artistName: String,
        sourceUrl: String
    ): Lyrics {
        return playerRepository.generateAiTranslation(songName, artistName, sourceUrl)
    }
}
