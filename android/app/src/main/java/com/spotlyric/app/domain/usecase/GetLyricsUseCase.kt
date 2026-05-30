package com.spotlyric.app.domain.usecase

import com.spotlyric.app.domain.model.Lyrics
import com.spotlyric.app.domain.repository.PlayerRepository
import javax.inject.Inject

class GetLyricsUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(songName: String, artistName: String): Lyrics? {
        return playerRepository.getLyrics(songName, artistName)
    }
}
